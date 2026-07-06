package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Manager;
import com.company.task_manager.model.RegularEmployee;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.model.WorkerType;
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
    private PasswordEncoder passwordEncoder;

    public WorkerService(WorkerRepository workerRepository, TaskRepository taskRepository,
                          ManagerRepository managerRepository, PasswordEncoder passwordEncoder) {
        this.workerRepository = workerRepository;
        this.taskRepository = taskRepository;
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

    public Worker findById(int id, AppUserPrincipal principal){
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));
        if (!principal.isAdmin() && worker.getDepartment() != principal.getDepartment()) {
            throw new ForbiddenOperationException("You cannot view workers from another department");
        }
        return worker;
    }

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
        if (workerType == WorkerType.MANAGER && managerRepository.existsByDepartment(department)) {
            throw new IllegalArgumentException("Department " + department + " already has a manager");
        }

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

    public void deleteWorker(int id, AppUserPrincipal principal){
        Worker worker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));
        assertCanManage(worker, principal);
        workerRepository.delete(worker);
    }

    @Transactional
    public Worker updateWorker(int id, String name, Department department, String username, String password,
                                List<Integer> taskIds, AppUserPrincipal principal){
        Worker existingWorker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));

        assertCanManage(existingWorker, principal);
        if (principal.isManager() && department != principal.getDepartment()) {
            throw new ForbiddenOperationException("Managers cannot move a worker to another department");
        }

        existingWorker.setName(name); // odkiaľ zoberieš nové meno?
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

        return workerRepository.save(existingWorker); // ktorý objekt ukladáš?
    }

    private void assertCanManage(Worker worker, AppUserPrincipal principal) {
        if (principal.isAdmin()) return;
        if (principal.isManager() && worker.getDepartment() == principal.getDepartment() && !(worker instanceof Manager)) return;
        throw new ForbiddenOperationException("You are not allowed to manage this worker");
    }

    private void assertSameDepartment(Worker worker, Task task) {
        if (worker.getDepartment() != task.getDepartment()) {
            throw new IllegalArgumentException("Worker department must match the assigned task's department");
        }
    }
}
