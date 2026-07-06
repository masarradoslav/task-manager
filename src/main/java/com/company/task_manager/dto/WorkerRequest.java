package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// taskIds is the full desired set of assigned tasks: null leaves assignments untouched,
// otherwise tasks missing from the list are unassigned and tasks in it are assigned.
public record WorkerRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        @NotNull(message = "Department is required")
        Department department,
        List<Integer> taskIds
) {
}
