package com.company.task_manager.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

// taskIds is the full desired set of assigned tasks: null leaves assignments untouched,
// otherwise tasks missing from the list are unassigned and tasks in it are assigned.
public record WorkerRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        List<Integer> taskIds
) {
}
