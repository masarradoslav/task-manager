package com.company.task_manager.dto;

import com.company.task_manager.model.TaskPriority;
import com.company.task_manager.model.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        TaskStatus status,
        TaskPriority priority,
        @Future(message = "Due date must be in the future")
        LocalDateTime dueDate,
        Integer workerId
) {
}
