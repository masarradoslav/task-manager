package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.model.AdminAccount;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.RegularEmployee;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.AdminAccountRepository;
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
class AdminAccountServiceTest {

    @Mock
    private AdminAccountRepository adminAccountRepository;
    @Mock
    private WorkerRepository workerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminAccountService adminAccountService;

    private AdminAccount admin;
    private AppUserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        adminAccountService = new AdminAccountService(adminAccountRepository, workerRepository, passwordEncoder);
        admin = new AdminAccount("admin", "hashed-old-password");
        adminPrincipal = AppUserPrincipal.forAdmin(admin);
    }


    @Test
    void updateSelf_shouldThrowForbidden_whenPrincipalIsNotAdmin() {
        Worker employee = new RegularEmployee("Employee");
        employee.setUsername("emp");
        employee.setDepartment(Department.IT);
        AppUserPrincipal workerPrincipal = AppUserPrincipal.forWorker(employee);

        assertThrows(ForbiddenOperationException.class,
                () -> adminAccountService.updateSelf("newname", "pw", workerPrincipal));
        verify(adminAccountRepository, never()).save(any());
    }

    @Test
    void updateSelf_shouldThrowIllegalArgument_whenUsernameAlreadyTaken() {
        when(adminAccountRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(adminAccountRepository.findByUsername("taken")).thenReturn(Optional.of(new AdminAccount("taken", "x")));

        assertThrows(IllegalArgumentException.class,
                () -> adminAccountService.updateSelf("taken", null, adminPrincipal));
        verify(adminAccountRepository, never()).save(any());
    }

    @Test
    void updateSelf_shouldUpdateUsernameAndEncodePassword_whenValid() {
        when(adminAccountRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(adminAccountRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(workerRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpass123")).thenReturn("hashed-new-password");
        when(adminAccountRepository.save(admin)).thenReturn(admin);

        AdminAccount result = adminAccountService.updateSelf("newadmin", "newpass123", adminPrincipal);

        assertEquals("newadmin", result.getUsername());
        assertEquals("hashed-new-password", result.getPassword());
    }
}
