package com.company.task_manager.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkerRequest(
        @NotBlank(message = "Name must not be blank")
        String name
) {
}
