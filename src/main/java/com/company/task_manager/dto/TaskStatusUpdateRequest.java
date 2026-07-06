package com.company.task_manager.dto;

import com.company.task_manager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(
        @NotNull(message = "Status is required")
        TaskStatus status
) {
}
