package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import com.company.task_manager.security.AppUserPrincipal;

public record CurrentUserResponse(
        String username,
        String role,
        Integer workerId,
        String name,
        Department department
) {
    // Builds the "who am I" response DTO from the authenticated principal.
    public static CurrentUserResponse from(AppUserPrincipal principal) {
        return new CurrentUserResponse(
                principal.getUsername(),
                principal.getRole().name(),
                principal.getWorkerId(),
                principal.getDisplayName(),
                principal.getDepartment()
        );
    }
}
