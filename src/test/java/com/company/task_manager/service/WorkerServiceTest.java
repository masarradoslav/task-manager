package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.OptimisticLockConflictException;
import com.company.task_manager.model.AdminAccount;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Manager;
import com.company.task_manager.model.RegularEmployee;
import com.company.task_manager.model.Worker;
import com.company.task_manager.model.WorkerType;
import com.company.task_manager.repository.AdminAccountRepository;
import com.company.task_manager.repository.ManagerRepository;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import com.company.task_manager.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private AdminAccountRepository adminAccountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private WorkerService workerService;

    @BeforeEach
    void setUp() {
        workerService = new WorkerService(workerRepository, taskRepository, managerRepository,
                adminAccountRepository, passwordEncoder);
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
    void createWorker_shouldThrowForbidden_whenEmployeeCreates() {
        assertThrows(ForbiddenOperationException.class, () -> workerService.createWorker(
                "New", Department.IT, "newuser", "pw", WorkerType.EMPLOYEE, null,
                employeePrincipal(Department.IT)));
        verify(workerRepository, never()).save(any());
    }

    @Test
    void createWorker_shouldThrowForbidden_whenManagerCreatesInOtherDepartment() {
        assertThrows(ForbiddenOperationException.class, () -> workerService.createWorker(
                "New", Department.HR, "newuser", "pw", WorkerType.EMPLOYEE, null,
                managerPrincipal(Department.IT)));
        verify(workerRepository, never()).save(any());
    }

    @Test
    void createWorker_shouldThrowForbidden_whenManagerCreatesAnotherManager() {
        assertThrows(ForbiddenOperationException.class, () -> workerService.createWorker(
                "New", Department.IT, "newuser", "pw", WorkerType.MANAGER, null,
                managerPrincipal(Department.IT)));
        verify(workerRepository, never()).save(any());
    }

    @Test
    void createWorker_shouldThrowIllegalArgument_whenPasswordMissing() {
        assertThrows(IllegalArgumentException.class, () -> workerService.createWorker(
                "New", Department.IT, "newuser", null, WorkerType.EMPLOYEE, null, adminPrincipal()));
        verify(workerRepository, never()).save(any());
    }

    @Test
    void createWorker_shouldThrowIllegalArgument_whenWorkerTypeMissing() {
        assertThrows(IllegalArgumentException.class, () -> workerService.createWorker(
                "New", Department.IT, "newuser", "pw", null, null, adminPrincipal()));
        verify(workerRepository, never()).save(any());
    }

    @Test
    void createWorker_shouldThrowIllegalArgument_whenUsernameAlreadyTaken() {
        when(workerRepository.findByUsername("taken")).thenReturn(Optional.of(new RegularEmployee("Other")));

        assertThrows(IllegalArgumentException.class, () -> workerService.createWorker(
                "New", Department.IT, "taken", "pw", WorkerType.EMPLOYEE, null, adminPrincipal()));
        verify(workerRepository, never()).save(any());
    }

    @Test
    void createWorker_shouldCreateAndEncodePassword_whenValid() {
        when(workerRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(adminAccountRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pw")).thenReturn("encoded-pw");
        when(workerRepository.save(any(Worker.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Worker result = workerService.createWorker(
                "New", Department.IT, "newuser", "pw", WorkerType.EMPLOYEE, null, adminPrincipal());

        assertEquals("New", result.getName());
        assertEquals(Department.IT, result.getDepartment());
        assertEquals("newuser", result.getUsername());
        assertEquals("encoded-pw", result.getPassword());
    }


    @Test
    void getWorkers() {
    }

    @Test
    void findById() {
    }

    @Test
    void deleteWorker() {
    }

    @Test
    void updateWorker_shouldThrowIllegalArgument_whenVersionMissing() {
        Worker existingWorker = new RegularEmployee("Bob");
        existingWorker.setDepartment(Department.IT);
        when(workerRepository.findById(1)).thenReturn(Optional.of(existingWorker));

        assertThrows(IllegalArgumentException.class, () -> workerService.updateWorker(
                1, "Bob 2", Department.IT, "bob2", null, null, null, adminPrincipal()));
        verify(workerRepository, never()).save(any());
    }

    @Test
    void updateWorker_shouldThrowOptimisticLockConflict_whenVersionMismatches() {
        Worker existingWorker = new RegularEmployee("Bob");
        existingWorker.setDepartment(Department.IT);
        when(workerRepository.findById(1)).thenReturn(Optional.of(existingWorker));

        // existingWorker.getVersion() defaults to 0 for a freshly-constructed entity - 5 mismatches
        assertThrows(OptimisticLockConflictException.class, () -> workerService.updateWorker(
                1, "Bob 2", Department.IT, "bob2", null, null, 5L, adminPrincipal()));
        verify(workerRepository, never()).save(any());
    }

    @Test
    void updateWorker_shouldSave_whenVersionMatches() {
        Worker existingWorker = new RegularEmployee("Bob");
        existingWorker.setDepartment(Department.IT);
        existingWorker.setUsername("bob");
        when(workerRepository.findById(1)).thenReturn(Optional.of(existingWorker));
        when(workerRepository.findByUsername("bob2")).thenReturn(Optional.empty());
        when(adminAccountRepository.findByUsername("bob2")).thenReturn(Optional.empty());
        when(workerRepository.save(existingWorker)).thenReturn(existingWorker);

        Worker result = workerService.updateWorker(
                1, "Bob 2", Department.IT, "bob2", null, null, 0L, adminPrincipal());

        assertEquals("Bob 2", result.getName());
        verify(workerRepository).save(existingWorker);
    }
}
