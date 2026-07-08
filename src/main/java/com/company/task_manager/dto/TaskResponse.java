package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.TaskPriority;
import com.company.task_manager.model.TaskStatus;

import java.time.LocalDateTime;

// Plocha DTO - worker je referencovaný len cez id/name, nie celou entitou,
// takže nemôže dôjsť k nekonečnej rekurzii Task -> Worker -> tasks -> Task ...
public record TaskResponse(
        int id,
        String name,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,
        Department department,
        Integer workerId,
        String workerName
) {
    // Builds a response DTO from a Task entity, flattening the worker to id/name only.
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getDepartment(),
                task.getWorker() != null ? task.getWorker().getId() : null,
                task.getWorker() != null ? task.getWorker().getName() : null
        );
    }
}
