package com.company.task_manager.controller;

import com.company.task_manager.dto.TaskRequest;
import com.company.task_manager.dto.TaskResponse;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Collection<TaskResponse> getAllTasks(){
        return taskService.getAllTasks().stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{id}")
    public Optional<TaskResponse> getTaskById(@PathVariable int id){
        return taskService.findById(id).map(TaskResponse::from);
    }

    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request){
        Task saved = taskService.createTask(toEntity(request));
        return TaskResponse.from(saved);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable int id, @RequestBody TaskRequest request){
        Task updated = taskService.updateTask(id, toEntity(request));
        return updated != null ? TaskResponse.from(updated) : null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    private Task toEntity(TaskRequest request) {
        Task task = new Task();
        task.setName(request.name());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        if (request.workerId() != null) {
            task.setWorker(new Worker(request.workerId()));
        }
        return task;
    }

}
