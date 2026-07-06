package com.company.task_manager.controller;

import com.company.task_manager.dto.PageResponse;
import com.company.task_manager.dto.TaskRequest;
import com.company.task_manager.dto.TaskResponse;
import com.company.task_manager.dto.TaskStatusUpdateRequest;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.TaskPriority;
import com.company.task_manager.model.TaskStatus;
import com.company.task_manager.model.Worker;
import com.company.task_manager.security.AppUserPrincipal;
import com.company.task_manager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public PageResponse<TaskResponse> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Department department,
            @RequestParam(required = false) Boolean overdueOnly,
            @RequestParam(required = false) Integer workerId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ){
        Page<Task> result = taskService.getTasks(page, size, status, priority, department, overdueOnly, workerId, principal);
        return PageResponse.from(result.map(TaskResponse::from));
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable int id, @AuthenticationPrincipal AppUserPrincipal principal){
        return TaskResponse.from(taskService.findById(id, principal));
    }

    @PostMapping
    public TaskResponse createTask(@Valid @RequestBody TaskRequest request, @AuthenticationPrincipal AppUserPrincipal principal){
        Task saved = taskService.createTask(toEntity(request), principal);
        return TaskResponse.from(saved);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable int id, @Valid @RequestBody TaskRequest request,
                                    @AuthenticationPrincipal AppUserPrincipal principal){
        Task updated = taskService.updateTask(id, toEntity(request), principal);
        return TaskResponse.from(updated);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateTaskStatus(@PathVariable int id, @Valid @RequestBody TaskStatusUpdateRequest request,
                                          @AuthenticationPrincipal AppUserPrincipal principal){
        Task updated = taskService.updateStatus(id, request.status(), principal);
        return TaskResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id, @AuthenticationPrincipal AppUserPrincipal principal){
        taskService.deleteTask(id, principal);
        return ResponseEntity.noContent().build();
    }

    private Task toEntity(TaskRequest request) {
        Task task = new Task();
        task.setName(request.name());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setDepartment(request.department());
        if (request.workerId() != null) {
            task.setWorker(new Worker(request.workerId()));
        }
        return task;
    }

}
