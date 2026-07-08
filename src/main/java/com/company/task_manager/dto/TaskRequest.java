package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.TaskPriority;
import com.company.task_manager.model.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// Incoming payload for creating/updating a task. All fields are required except workerId:
// on a PUT it carries the full desired assignment - an id (re)assigns, null/absent unassigns.
public record TaskRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        @NotNull(message = "Status is required")
        TaskStatus status,
        @NotNull(message = "Priority is required")
        TaskPriority priority,
        @NotNull(message = "Due date is required")
        @Future(message = "Due date must be in the future")
        LocalDateTime dueDate,
        @NotNull(message = "Department is required")
        Department department,
        Integer workerId
) {
}
