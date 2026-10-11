package com.company.task_manager.controller;

import com.company.task_manager.dto.CommentRequest;
import com.company.task_manager.dto.CommentResponse;
import com.company.task_manager.model.Comment;
import com.company.task_manager.security.AppUserPrincipal;
import com.company.task_manager.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // GET /tasks/{taskId}/comments - a task's comments, latest first.
    @GetMapping("/tasks/{taskId}/comments")
    public List<CommentResponse> getComments(@PathVariable int taskId, @AuthenticationPrincipal AppUserPrincipal principal) {
        return commentService.getCommentsForTask(taskId, principal).stream()
                .map(CommentResponse::from)
                .toList();
    }

    // POST /tasks/{taskId}/comments - add a comment (author derived from the logged-in principal).
    @PostMapping("/tasks/{taskId}/comments")
    public CommentResponse createComment(@PathVariable int taskId, @Valid @RequestBody CommentRequest request,
                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        Comment saved = commentService.createComment(taskId, toEntity(request), principal);
        return CommentResponse.from(saved);
    }

    // PUT /comments/{id} - edit a comment's name/content (author only).
    @PutMapping("/comments/{id}")
    public CommentResponse updateComment(@PathVariable long id, @Valid @RequestBody CommentRequest request,
                                          @AuthenticationPrincipal AppUserPrincipal principal) {
        Comment updated = commentService.updateComment(id, toEntity(request), principal);
        return CommentResponse.from(updated);
    }

    // DELETE /comments/{id} - remove a comment (author or Admin).
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        commentService.deleteComment(id, principal);
        return ResponseEntity.noContent().build();
    }

    // Maps the request DTO to a Comment entity (task/worker/dates are set by the service).
    private Comment toEntity(CommentRequest request) {
        return new Comment(request.name(), request.content());
    }
}
