package com.company.task_manager.dto;

import com.company.task_manager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

// data for PATCH /tasks/{id}/status, the only task field employee can change
public record TaskStatusUpdateRequest(
        @NotNull(message = "Status is required")
        TaskStatus status
) {
}
