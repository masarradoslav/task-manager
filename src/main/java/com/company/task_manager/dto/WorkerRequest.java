package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.WorkerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// password is intentionally NOT @NotBlank: required on create (checked manually in
// WorkerService.createWorker, since it's never sent back to prefill an edit form) but
// optional on update, where a blank/omitted value means "leave the password unchanged".
// workerType is only used on create - a worker's Manager/RegularEmployee subtype is fixed
// at creation and ignored on update. taskIds is the full desired set of assigned tasks.
public record WorkerRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        @NotNull(message = "Department is required")
        Department department,
        @NotBlank(message = "Username must not be blank")
        String username,
        String password,
        @NotNull(message = "Worker type is required")
        WorkerType workerType,
        List<Integer> taskIds
) {
}
