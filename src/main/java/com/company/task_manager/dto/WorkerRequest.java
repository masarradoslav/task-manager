package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.WorkerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record WorkerRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        @NotNull(message = "Department is required")
        Department department,
        @NotBlank(message = "Username must not be blank")
        String username,
        String password,
        WorkerType workerType,
        List<Integer> taskIds,
        Long version
) {
}
