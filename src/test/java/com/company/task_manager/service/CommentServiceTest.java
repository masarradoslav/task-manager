package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.AdminAccount;
import com.company.task_manager.model.Comment;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.RegularEmployee;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.CommentRepository;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import com.company.task_manager.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private WorkerRepository workerRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository, taskRepository, workerRepository);
    }

    private AppUserPrincipal adminPrincipal() {
        return AppUserPrincipal.forAdmin(new AdminAccount("admin", "hash"));
    }

    private AppUserPrincipal employeePrincipal(Department department) {
        Worker employee = new RegularEmployee("Employee");
        employee.setUsername("emp");
        employee.setDepartment(department);
        return AppUserPrincipal.forWorker(employee);
    }

    private Task taskInDepartment(Department department) {
        Task task = new Task("Some task");
        task.setDepartment(department);
        return task;
    }

    @Test
    void createComment_shouldThrowForbidden_whenAdminCreates() {
        // rejected before the task is even looked up - Admin has no Worker identity to author with
        assertThrows(ForbiddenOperationException.class,
                () -> commentService.createComment(1, new Comment("n", "c"), adminPrincipal()));
        verify(taskRepository, never()).findById(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_shouldThrowForbidden_whenWorkerCommentsOnOtherDepartmentTask() {
        when(taskRepository.findById(1)).thenReturn(Optional.of(taskInDepartment(Department.HR)));

        assertThrows(ForbiddenOperationException.class,
                () -> commentService.createComment(1, new Comment("n", "c"), employeePrincipal(Department.IT)));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_shouldThrowNotFound_whenTaskDoesNotExist() {
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.createComment(1, new Comment("n", "c"), employeePrincipal(Department.IT)));
    }

    @Test
    void createComment_shouldSetAuthorAndDateCreated_whenEmployeeCommentsInOwnDepartment() {
        Task task = taskInDepartment(Department.IT);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));
        Worker author = new RegularEmployee("Employee");
        author.setDepartment(Department.IT);
        when(workerRepository.findById(0)).thenReturn(Optional.of(author));
        Comment comment = new Comment("n", "c");
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.createComment(1, comment, employeePrincipal(Department.IT));

        assertSame(task, result.getTask());
        assertSame(author, result.getWorker());
        assertNotNull(result.getDateCreated());
        verify(commentRepository).save(comment);
    }

    @Test
    void getCommentsForTask_shouldThrowForbidden_whenNonAdminViewsOtherDepartment() {
        when(taskRepository.findById(1)).thenReturn(Optional.of(taskInDepartment(Department.HR)));

        assertThrows(ForbiddenOperationException.class,
                () -> commentService.getCommentsForTask(1, employeePrincipal(Department.IT)));
    }

    @Test
    void updateComment_shouldThrowForbidden_whenNotAuthor() {
        Comment comment = new Comment("n", "c");
        comment.setWorker(new Worker(5));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(ForbiddenOperationException.class,
                () -> commentService.updateComment(1, new Comment("n2", "c2"), employeePrincipal(Department.IT)));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_shouldSetDateLastEdit_whenAuthorEdits() {
        Worker author = new RegularEmployee("Employee");
        author.setDepartment(Department.IT);
        AppUserPrincipal principal = AppUserPrincipal.forWorker(author);
        Comment comment = new Comment("old name", "old content");
        comment.setWorker(author);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.updateComment(1, new Comment("new name", "new content"), principal);

        assertEquals("new name", result.getName());
        assertEquals("new content", result.getContent());
        assertNotNull(result.getDateLastEdit());
    }

    @Test
    void deleteComment_shouldThrowForbidden_whenNotAuthorAndNotAdmin() {
        Comment comment = new Comment("n", "c");
        comment.setWorker(new Worker(5));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(ForbiddenOperationException.class,
                () -> commentService.deleteComment(1, employeePrincipal(Department.IT)));
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_shouldSucceed_whenAuthorDeletesOwnComment() {
        Worker author = new RegularEmployee("Employee");
        author.setDepartment(Department.IT);
        AppUserPrincipal principal = AppUserPrincipal.forWorker(author);
        Comment comment = new Comment("n", "c");
        comment.setWorker(author);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1, principal);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_shouldSucceed_whenAdminDeletes() {
        Comment comment = new Comment("n", "c");
        comment.setWorker(new Worker(5));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1, adminPrincipal());

        verify(commentRepository).delete(comment);
    }
}
