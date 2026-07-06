package com.company.task_manager.service;

import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.TaskPriority;
import com.company.task_manager.model.TaskStatus;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                                Department department, Boolean overdueOnly, Integer workerId){
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        Specification<Task> spec = buildFilter(status, priority, department, overdueOnly, workerId);
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

    public Task findById(int id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    public void deleteTask(int id){
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found: " + id);
        }
        taskRepository.deleteById(id);
    }

    public Task createTask(Task task){
        if (task.getWorker() != null) {
            Optional<Worker> workerOpt = workerRepository.findById(task.getWorker().getId());
            task.setWorker(workerOpt.orElse(null));
        }

        if (task.getWorker() != null) {
            assertSameDepartment(task, task.getWorker());
        }

        return taskRepository.save(task);
    }


    public Task updateTask(int id, Task updatedData){
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        existingTask.setName(updatedData.getName()); // odkiaľ zoberieš nové meno?
        existingTask.setStatus(updatedData.getStatus());
        existingTask.setPriority(updatedData.getPriority());
        existingTask.setDueDate(updatedData.getDueDate());
        existingTask.setDepartment(updatedData.getDepartment());

        if (updatedData.getWorker() != null) {
            int workerId = updatedData.getWorker().getId();
            Optional<Worker> workerOpt = workerRepository.findById(workerId);

            if (workerOpt.isPresent()) {
                existingTask.setWorker(workerOpt.get());
            }
        }

        // re-checked unconditionally (not just when the worker changed this request) so a
        // department change alone can't leave a task assigned to a worker in another department
        if (existingTask.getWorker() != null) {
            assertSameDepartment(existingTask, existingTask.getWorker());
        }

        return taskRepository.save(existingTask); // ktorý objekt ukladáš?
    }

    private void assertSameDepartment(Task task, Worker worker) {
        if (task.getDepartment() != worker.getDepartment()) {
            throw new IllegalArgumentException("Task department must match the assigned worker's department");
        }
    }
}
