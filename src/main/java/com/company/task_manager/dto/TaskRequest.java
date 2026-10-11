package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.TaskPriority;
import com.company.task_manager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        @NotNull(message = "Status is required")
        TaskStatus status,
        @NotNull(message = "Priority is required")
        TaskPriority priority,
        LocalDateTime dueDate,
        @NotNull(message = "Department is required")
        Department department,
        Integer workerId,
        Long version
) {
}
