package com.company.task_manager.dto;

import com.company.task_manager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

// Payload for PATCH /tasks/{id}/status - the only task edit a regular employee may make.
public record TaskStatusUpdateRequest(
        @NotNull(message = "Status is required")
        TaskStatus status
) {
}
