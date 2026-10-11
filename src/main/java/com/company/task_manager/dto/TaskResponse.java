package com.company.task_manager.dto;

import com.company.task_manager.model.*;

import java.time.LocalDateTime;


public record TaskResponse(
        int id,
        String name,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,
        Department department,
        Integer workerId,
        String workerName,
        long version
) {
    // builds a response DTO from a Task entity, worker is represented by name and id only
    public static TaskResponse from(Task task) {
        Worker worker = task.getWorker();
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getDepartment(),
                worker != null ? worker.getId() : null,
                worker != null ? worker.getName() : null,
                task.getVersion()
        );
    }
}
