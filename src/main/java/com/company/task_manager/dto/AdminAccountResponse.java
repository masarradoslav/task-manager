package com.company.task_manager.dto;

import com.company.task_manager.model.AdminAccount;

public record AdminAccountResponse(String username) {
    // Builds the response DTO from the entity, does not include the password hash
    public static AdminAccountResponse from(AdminAccount admin) {
        return new AdminAccountResponse(admin.getUsername());
    }
}
