package com.company.task_manager.dto;

import jakarta.validation.constraints.NotBlank;

// password is intentionally NOT @NotBlank: a blank/omitted value means "leave the password
// unchanged", same convention as WorkerRequest.
public record AdminAccountRequest(
        @NotBlank(message = "Username must not be blank")
        String username,
        String password
) {
}
