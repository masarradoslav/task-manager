package com.company.task_manager.dto;

import com.company.task_manager.model.TaskPriority;
import com.company.task_manager.model.TaskStatus;

import java.time.LocalDateTime;

public record TaskRequest(
        String name,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,
        Integer workerId
) {
}
