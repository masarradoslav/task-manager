package com.company.task_manager.controller;

import com.company.task_manager.model.Task;
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
    public Collection<Task> getAllTasks(){
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Optional<Task> getTaskById(@PathVariable int id){
        return taskService.findById(id);
    }

    @PostMapping
    public Task createTask(@RequestBody Task task){
        Task t = taskService.createTask(task);
        return t;
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable int id, @RequestBody Task update){
        return taskService.updateTask(id, update);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable int id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}
