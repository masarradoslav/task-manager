package com.company.task_manager.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

// taskIds is only honoured on creation (assign existing tasks to a new worker); ignored on update.
public record WorkerRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        List<Integer> taskIds
) {
}
