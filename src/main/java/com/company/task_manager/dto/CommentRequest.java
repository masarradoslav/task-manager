package com.company.task_manager.dto;

import jakarta.validation.constraints.NotBlank;

// incoming data for creating/editing a comment; author and task are never client-supplied
// (derived server-side from the authenticated principal / URL path), only name and content are
public record CommentRequest(
        @NotBlank(message = "Name must not be blank")
        String name,
        @NotBlank(message = "Content must not be blank")
        String content
) {
}
