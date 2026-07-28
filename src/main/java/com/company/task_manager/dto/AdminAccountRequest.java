package com.company.task_manager.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminAccountRequest(
        @NotBlank(message = "Username must not be blank")
        String username,

        //password does not have @NotBlank, because empty password means do not change it
        String password
) {
}
