package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Manager;
import com.company.task_manager.model.RegularEmployee;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.model.WorkerType;
import com.company.task_manager.repository.AdminAccountRepository;
import com.company.task_manager.repository.ManagerRepository;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import com.company.task_manager.security.AppUserPrincipal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WorkerService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private WorkerRepository workerRepository;
    private TaskRepository taskRepository;
    private ManagerRepository managerRepository;
    private AdminAccountRepository adminAccountRepository;
    private PasswordEncoder passwordEncoder;

    public WorkerService(WorkerRepository workerRepository, TaskRepository taskRepository,
                          ManagerRepository managerRepository, AdminAccountRepository adminAccountRepository,
                          PasswordEncoder passwordEncoder) {
        this.workerRepository = workerRepository;
        this.taskRepository = taskRepository;
        this.managerRepository = managerRepository;
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Returns a page of workers; non-admins are always scoped to their own department.
    public Page<Worker> getWorkers(int page, int size, Department department, AppUserPrincipal principal){
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        Department effectiveDepartment = principal.isAdmin() ? department : principal.getDepartment();

        Specification<Worker> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (effectiveDepartment != null) predicates.add(cb.equal(root.get("department"), effectiveDepartment));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return workerRepository.findAll(spec, PageRequest.of(safePage, safeSize));
    }

    // Loads one worker, allowing non-admins to see only their own department.
    public Worker findById(int id, AppUserPrincipal principal){
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));
        if (!principal.isAdmin() && worker.getDepartment() != principal.getDepartment()) {
            throw new ForbiddenOperationException("You cannot view workers from another department");
        }
        return worker;
    }

    // Creates a worker (Manager or RegularEmployee) with optional initial task assignments.
    // Managers may only create employees in their own department; admins may create anyone.
    @Transactional
    public Worker createWorker(String name, Department department, String username, String password,
                                WorkerType workerType, List<Integer> taskIds, AppUserPrincipal principal){
        if (principal.isEmployee()) {
            throw new ForbiddenOperationException("Employees cannot create workers");
        }
        if (principal.isManager()) {
            if (department != principal.getDepartment()) {
                throw new ForbiddenOperationException("Managers can only create workers in their own department");
            }
            if (workerType == WorkerType.MANAGER) {
                throw new ForbiddenOperationException("Managers cannot create other managers");
            }
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (workerType == null) {
            throw new IllegalArgumentException("Worker type is required");
        }
        if (workerType == WorkerType.MANAGER && managerRepository.existsByDepartment(department)) {
            throw new IllegalArgumentException("Department " + department + " already has a manager");
        }
        assertUsernameAvailable(username, null);

        Worker worker = workerType == WorkerType.MANAGER ? new Manager(name) : new RegularEmployee(name);
        worker.setDepartment(department);
        worker.setUsername(username);
        worker.setPassword(passwordEncoder.encode(password));

        Worker savedWorker = workerRepository.save(worker);

        if (taskIds != null) {
            for (Integer taskId : taskIds) {
                Task task = taskRepository.findById(taskId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
                assertSameDepartment(savedWorker, task);
                savedWorker.addTask(task);
                taskRepository.save(task);
            }
        }

        return savedWorker;
    }

    // Deletes a worker if the principal is allowed to manage them.
    public void deleteWorker(int id, AppUserPrincipal principal){
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));
        assertCanManage(worker, principal);
        workerRepository.delete(worker);
    }

    // Updates a worker: a self-edit may only change name/username/password, a managing
    // edit may also move departments and replace the full set of assigned tasks.
    @Transactional
    public Worker updateWorker(int id, String name, Department department, String username, String password,
                                List<Integer> taskIds, AppUserPrincipal principal){
        Worker existingWorker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));

        // Self-service edit: an Employee or Manager editing their own record may only change
        // name/username/password. Department and taskIds are deliberately never read on this
        // path, so a worker can never move their own department or unassign their own tasks.
        if (isSelfEdit(existingWorker, principal)) {
            assertUsernameAvailable(username, existingWorker.getId());
            existingWorker.setName(name);
            existingWorker.setUsername(username);
            if (password != null && !password.isBlank()) {
                existingWorker.setPassword(passwordEncoder.encode(password));
            }
            return workerRepository.save(existingWorker);
        }

        assertCanManage(existingWorker, principal);
        if (principal.isManager() && department != principal.getDepartment()) {
            throw new ForbiddenOperationException("Managers cannot move a worker to another department");
        }
        assertUsernameAvailable(username, existingWorker.getId());

        existingWorker.setName(name);
        existingWorker.setDepartment(department);
        existingWorker.setUsername(username);
        if (password != null && !password.isBlank()) {
            existingWorker.setPassword(passwordEncoder.encode(password));
        }

        if (taskIds != null) {
            Set<Integer> desiredIds = new HashSet<>(taskIds);

            for (Task task : new ArrayList<>(existingWorker.getTasks())) {
                if (!desiredIds.contains(task.getId())) {
                    existingWorker.getTasks().remove(task);
                    task.setWorker(null);
                    taskRepository.save(task);
                }
            }

            for (Integer taskId : desiredIds) {
                boolean alreadyAssigned = existingWorker.getTasks().stream()
                        .anyMatch(task -> task.getId() == taskId);
                if (!alreadyAssigned) {
                    Task task = taskRepository.findById(taskId)
                            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
                    assertSameDepartment(existingWorker, task);
                    existingWorker.addTask(task);
                    taskRepository.save(task);
                }
            }
        }

        // re-checked unconditionally (covers tasks kept from before this update, in case the
        // worker's department just changed) so no task can end up in another department's worker
        for (Task task : existingWorker.getTasks()) {
            assertSameDepartment(existingWorker, task);
        }

        return workerRepository.save(existingWorker);
    }

    // Admin manages everyone; a manager manages only non-manager workers of their department.
    private void assertCanManage(Worker worker, AppUserPrincipal principal) {
        if (principal.isAdmin()) return;
        if (principal.isManager() && worker.getDepartment() == principal.getDepartment() && !(worker instanceof Manager)) return;
        throw new ForbiddenOperationException("You are not allowed to manage this worker");
    }

    // True when a non-admin Employee/Manager is updating their own worker record (not someone else's).
    private boolean isSelfEdit(Worker worker, AppUserPrincipal principal) {
        return !principal.isAdmin() && principal.getWorkerId() != null && principal.getWorkerId() == worker.getId();
    }

    // A task may only ever be assigned to a worker of the same department.
    private void assertSameDepartment(Worker worker, Task task) {
        if (worker.getDepartment() != task.getDepartment()) {
            throw new IllegalArgumentException("Worker department must match the assigned task's department");
        }
    }

    // Rejects a username already used by another worker or by the admin account - login resolves
    // admin first, then worker, so a duplicate would either fail the DB unique constraint (worker
    // vs worker) or silently shadow the worker's login (worker vs admin).
    private void assertUsernameAvailable(String username, Integer currentWorkerId) {
        boolean takenByOtherWorker = workerRepository.findByUsername(username)
                .map(other -> currentWorkerId == null || other.getId() != currentWorkerId)
                .orElse(false);
        if (takenByOtherWorker || adminAccountRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }
    }
}
