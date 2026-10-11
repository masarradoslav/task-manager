package com.company.task_manager.dto;

import com.company.task_manager.model.Comment;
import com.company.task_manager.model.Worker;

import java.time.LocalDateTime;

public record CommentResponse(
        long id,
        String name,
        String content,
        LocalDateTime dateCreated,
        LocalDateTime dateLastEdit,
        int taskId,
        int workerId,
        String workerName
) {
    // builds a response DTO from a Comment entity, author represented by id and name only
    public static CommentResponse from(Comment comment) {
        Worker worker = comment.getWorker();
        return new CommentResponse(
                comment.getId(),
                comment.getName(),
                comment.getContent(),
                comment.getDateCreated(),
                comment.getDateLastEdit(),
                comment.getTask().getId(),
                worker.getId(),
                worker.getName()
        );
    }
}
