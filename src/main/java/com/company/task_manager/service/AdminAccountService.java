package com.company.task_manager.service;

import com.company.task_manager.exception.ForbiddenOperationException;
import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.AdminAccount;
import com.company.task_manager.repository.AdminAccountRepository;
import com.company.task_manager.repository.WorkerRepository;
import com.company.task_manager.security.AppUserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAccountService {
    private final AdminAccountRepository adminAccountRepository;
    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(AdminAccountRepository adminAccountRepository, WorkerRepository workerRepository,
                                PasswordEncoder passwordEncoder) {
        this.adminAccountRepository = adminAccountRepository;
        this.workerRepository = workerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Lets an Admin change their own username/password. Looked up by id (not the possibly-stale
    // username cached in the session principal) so a second self-edit in the same session still works.
    public AdminAccount updateSelf(String username, String password, AppUserPrincipal principal) {
        if (!principal.isAdmin()) {
            throw new ForbiddenOperationException("Only an Admin can use this endpoint");
        }
        AdminAccount admin = adminAccountRepository.findById(principal.getAdminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin account not found"));

        // Checked against workers too: login resolves admin first, so an admin taking a worker's
        // username would silently lock that worker out.
        boolean takenByOtherAdmin = !username.equals(admin.getUsername())
                && adminAccountRepository.findByUsername(username).isPresent();
        if (takenByOtherAdmin || workerRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }
        admin.setUsername(username);
        if (password != null && !password.isBlank()) {
            admin.setPassword(passwordEncoder.encode(password));
        }
        return adminAccountRepository.save(admin);
    }
}
