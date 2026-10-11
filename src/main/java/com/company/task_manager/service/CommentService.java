package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Comment;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.CommentRepository;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import com.company.task_manager.security.AppUserPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final WorkerRepository workerRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository,
                           WorkerRepository workerRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.workerRepository = workerRepository;
    }

    // lists a task's comments, latest first; non-admins may only view their own department's tasks
    public List<Comment> getCommentsForTask(int taskId, AppUserPrincipal principal) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        assertCanView(task, principal);
        return commentRepository.findByTaskIdOrderByDateCreatedDesc(taskId);
    }

    // creates a comment on a task; Admin accounts have no Worker identity to author a comment with
    public Comment createComment(int taskId, Comment comment, AppUserPrincipal principal) {
        if (principal.isAdmin()) {
            throw new ForbiddenOperationException("Admin accounts cannot post comments");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        assertCanView(task, principal);

        // the author is always the logged-in worker, never something the client can specify
        Worker author = workerRepository.findById(principal.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + principal.getWorkerId()));

        comment.setTask(task);
        comment.setWorker(author);
        comment.setDateCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    // edits a comment's name/content; only the original author may edit their own comment
    public Comment updateComment(long id, Comment updatedData, AppUserPrincipal principal) {
        Comment existing = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));

        if (!isAuthor(existing, principal)) {
            throw new ForbiddenOperationException("You can only edit your own comments");
        }

        existing.setName(updatedData.getName());
        existing.setContent(updatedData.getContent());
        existing.setDateLastEdit(LocalDateTime.now());
        return commentRepository.save(existing);
    }

    // deletes a comment; allowed for its original author, or Admin acting as moderator
    public void deleteComment(long id, AppUserPrincipal principal) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));

        if (!principal.isAdmin() && !isAuthor(comment, principal)) {
            throw new ForbiddenOperationException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    private boolean isAuthor(Comment comment, AppUserPrincipal principal) {
        return principal.getWorkerId() != null && comment.getWorker().getId() == principal.getWorkerId();
    }

    // non-admins may only view/comment on tasks of their own department
    private void assertCanView(Task task, AppUserPrincipal principal) {
        if (!principal.isAdmin() && task.getDepartment() != principal.getDepartment()) {
            throw new ForbiddenOperationException("You cannot view or comment on tasks from another department");
        }
    }
}
