package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.TaskPriority;
import com.company.task_manager.model.TaskStatus;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import com.company.task_manager.security.AppUserPrincipal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private TaskRepository taskRepository;
    private WorkerRepository workerRepository;


    public TaskService(TaskRepository taskRepository, WorkerRepository workerRepository) {
        this.taskRepository = taskRepository;
        this.workerRepository = workerRepository;
    }

    public Page<Task> getTasks(int page, int size, TaskStatus status, TaskPriority priority,
                                Department department, Boolean overdueOnly, Integer workerId,
                                AppUserPrincipal principal){
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        // Manager/Employee can never see another department, regardless of what they pass in
        Department effectiveDepartment = principal.isAdmin() ? department : principal.getDepartment();

        Specification<Task> spec = buildFilter(status, priority, effectiveDepartment, overdueOnly, workerId);
        return taskRepository.findAll(spec, PageRequest.of(safePage, safeSize));
    }

    private Specification<Task> buildFilter(TaskStatus status, TaskPriority priority,
                                             Department department, Boolean overdueOnly, Integer workerId){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (priority != null) predicates.add(cb.equal(root.get("priority"), priority));
            if (department != null) predicates.add(cb.equal(root.get("department"), department));
            if (workerId != null) predicates.add(cb.equal(root.get("worker").get("id"), workerId));
            if (Boolean.TRUE.equals(overdueOnly)) {
                predicates.add(cb.lessThan(root.get("dueDate"), LocalDateTime.now()));
                predicates.add(cb.notEqual(root.get("status"), TaskStatus.DONE));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Task findById(int id, AppUserPrincipal principal){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        assertCanView(task, principal);
        return task;
    }

    public void deleteTask(int id, AppUserPrincipal principal){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        assertCanManage(task, principal);
        taskRepository.delete(task);
    }

    public Task createTask(Task task, AppUserPrincipal principal){
        if (principal.isEmployee()) {
            throw new ForbiddenOperationException("Employees cannot create tasks");
        }
        if (principal.isManager() && task.getDepartment() != principal.getDepartment()) {
            throw new ForbiddenOperationException("Managers can only create tasks in their own department");
        }

        // A nonexistent workerId is a 404, not a silently unassigned task
        if (task.getWorker() != null) {
            int workerId = task.getWorker().getId();
            Worker worker = workerRepository.findById(workerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + workerId));
            task.setWorker(worker);
            assertSameDepartment(task, worker);
        }

        return taskRepository.save(task);
    }


    public Task updateTask(int id, Task updatedData, AppUserPrincipal principal){
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        if (principal.isEmployee()) {
            throw new ForbiddenOperationException("Employees can only update a task's status");
        }
        if (principal.isManager()) {
            if (existingTask.getDepartment() != principal.getDepartment()) {
                throw new ForbiddenOperationException("Managers can only edit tasks in their own department");
            }
            if (updatedData.getDepartment() != principal.getDepartment()) {
                throw new ForbiddenOperationException("Managers cannot move a task to another department");
            }
        }

        existingTask.setName(updatedData.getName()); // odkiaľ zoberieš nové meno?
        existingTask.setStatus(updatedData.getStatus());
        existingTask.setPriority(updatedData.getPriority());
        existingTask.setDueDate(updatedData.getDueDate());
        existingTask.setDepartment(updatedData.getDepartment());

        // PUT carries the full desired state, so workerId decides the assignment either way:
        // present = (re)assign, absent/null = unassign. A nonexistent workerId is a 404,
        // not a silent no-op.
        if (updatedData.getWorker() != null) {
            int workerId = updatedData.getWorker().getId();
            Worker worker = workerRepository.findById(workerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + workerId));
            existingTask.setWorker(worker);
        } else {
            existingTask.setWorker(null);
        }

        // re-checked unconditionally (not just when the worker changed this request) so a
        // department change alone can't leave a task assigned to a worker in another department
        if (existingTask.getWorker() != null) {
            assertSameDepartment(existingTask, existingTask.getWorker());
        }

        return taskRepository.save(existingTask); // ktorý objekt ukladáš?
    }

    // The one edit a regular employee is allowed to make: the status of a task assigned
    // directly to them. Admin/Manager can also use this (subject to the same department
    // scoping as a full edit) since it's just a narrower version of updateTask.
    public Task updateStatus(int id, TaskStatus status, AppUserPrincipal principal){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        if (principal.isEmployee()) {
            boolean assignedToSelf = task.getWorker() != null
                    && principal.getWorkerId() != null
                    && task.getWorker().getId() == principal.getWorkerId();
            if (!assignedToSelf) {
                throw new ForbiddenOperationException("You can only update the status of tasks assigned to you");
            }
        } else {
            assertCanManage(task, principal);
        }

        task.setStatus(status);
        return taskRepository.save(task);
    }

    private void assertCanView(Task task, AppUserPrincipal principal) {
        if (!principal.isAdmin() && task.getDepartment() != principal.getDepartment()) {
            throw new ForbiddenOperationException("You cannot view tasks from another department");
        }
    }

    private void assertCanManage(Task task, AppUserPrincipal principal) {
        if (principal.isAdmin()) return;
        if (principal.isManager() && task.getDepartment() == principal.getDepartment()) return;
        throw new ForbiddenOperationException("You are not allowed to manage this task");
    }

    private void assertSameDepartment(Task task, Worker worker) {
        if (task.getDepartment() != worker.getDepartment()) {
            throw new IllegalArgumentException("Task department must match the assigned worker's department");
        }
    }
}
