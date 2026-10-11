package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.OptimisticLockConflictException;
import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.AdminAccount;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Manager;
import com.company.task_manager.model.RegularEmployee;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import com.company.task_manager.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private WorkerRepository workerRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, workerRepository);
    }

    private AppUserPrincipal adminPrincipal() {
        return AppUserPrincipal.forAdmin(new AdminAccount("admin", "hash"));
    }

    private AppUserPrincipal managerPrincipal(Department department) {
        Worker manager = new Manager("Manager");
        manager.setUsername("mgr");
        manager.setDepartment(department);
        return AppUserPrincipal.forWorker(manager);
    }

    private AppUserPrincipal employeePrincipal(Department department) {
        Worker employee = new RegularEmployee("Employee");
        employee.setUsername("emp");
        employee.setDepartment(department);
        return AppUserPrincipal.forWorker(employee);
    }



    @Test
    void createTask_shouldThrowForbidden_whenEmployeeCreates() {
        Task task = new Task("New task");
        task.setDepartment(Department.IT);

        assertThrows(ForbiddenOperationException.class,
                () -> taskService.createTask(task, employeePrincipal(Department.IT)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldThrowForbidden_whenManagerCreatesInOtherDepartment() {
        Task task = new Task("New task");
        task.setDepartment(Department.HR);

        assertThrows(ForbiddenOperationException.class,
                () -> taskService.createTask(task, managerPrincipal(Department.IT)));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_shouldSave_whenAdminCreates() {
        Task task = new Task("New task");
        task.setDepartment(Department.ACCOUNTING);
        task.setDueDate(LocalDateTime.now().plusDays(1));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task, adminPrincipal());

        assertSame(task, result);
        verify(taskRepository).save(task);
    }

    @Test
    void createTask_shouldSave_whenManagerCreatesInOwnDepartment() {
        Task task = new Task("New task");
        task.setDepartment(Department.IT);
        task.setDueDate(LocalDateTime.now().plusDays(1));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.createTask(task, managerPrincipal(Department.IT));

        assertSame(task, result);
        verify(taskRepository).save(task);
    }

    @Test
    void createTask_shouldThrowIllegalArgument_whenAssignedWorkerDepartmentMismatches() {
        Task task = new Task("New task");
        task.setDepartment(Department.IT);
        task.setDueDate(LocalDateTime.now().plusDays(1));
        // placeholder referencing an existing worker by id - the service resolves it via the repository
        task.setWorker(new Worker(5));
        Worker mismatchedWorker = new RegularEmployee("Bob");
        mismatchedWorker.setDepartment(Department.HR);
        when(workerRepository.findById(5)).thenReturn(Optional.of(mismatchedWorker));

        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(task, adminPrincipal()));
        verify(taskRepository, never()).save(any());
    }


    @Test
    void getTasks_shouldCallFindAll_whenNonAdminOmitsDepartment() {
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        taskService.getTasks(0, 20, null, null, null, null, null, employeePrincipal(Department.IT));

        verify(taskRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getTasks_shouldCallFindAll_whenAdminFiltersByDepartment() {
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        taskService.getTasks(0, 20, null, null, Department.HR, null, null, adminPrincipal());

        verify(taskRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findById_shouldThrowNotFound_whenTaskDoesNotExist() {
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.findById(1, adminPrincipal()));
    }

    @Test
    void findById_shouldThrowForbidden_whenNonAdminViewsOtherDepartment() {
        Task task = new Task("Some task");
        task.setDepartment(Department.HR);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenOperationException.class,
                () -> taskService.findById(1, managerPrincipal(Department.IT)));
    }

    @Test
    void findById_shouldReturnTask_whenNonAdminViewsOwnDepartment() {
        Task task = new Task("Some task");
        task.setDepartment(Department.IT);
        when(taskRepository.findById(1)).thenReturn(Optional.of(task));

        Task result = taskService.findById(1, employeePrincipal(Department.IT));

        assertSame(task, result);
    }

    @Test
    void updateTask_shouldThrowIllegalArgument_whenVersionMissing() {
        Task existingTask = new Task("Existing task");
        existingTask.setDepartment(Department.IT);
        when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));

        Task updatedData = new Task("Updated name");
        updatedData.setDepartment(Department.IT);

        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTask(1, updatedData, null, adminPrincipal()));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldThrowOptimisticLockConflict_whenVersionMismatches() {
        Task existingTask = new Task("Existing task");
        existingTask.setDepartment(Department.IT);
        when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));

        Task updatedData = new Task("Updated name");
        updatedData.setDepartment(Department.IT);

        // existingTask.getVersion() defaults to 0 for a freshly-constructed entity - 5 mismatches
        assertThrows(OptimisticLockConflictException.class,
                () -> taskService.updateTask(1, updatedData, 5L, adminPrincipal()));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_shouldSave_whenVersionMatches() {
        Task existingTask = new Task("Existing task");
        existingTask.setDepartment(Department.IT);
        when(taskRepository.findById(1)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        Task updatedData = new Task("Updated name");
        updatedData.setDepartment(Department.IT);

        Task result = taskService.updateTask(1, updatedData, 0L, adminPrincipal());

        assertEquals("Updated name", result.getName());
        verify(taskRepository).save(existingTask);
    }

    @Test
    void deleteTask() {
    }

    @Test
    void updateStatus() {
    }
}
