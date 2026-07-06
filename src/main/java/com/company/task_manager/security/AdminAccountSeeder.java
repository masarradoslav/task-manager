package com.company.task_manager.security;

import com.company.task_manager.model.AdminAccount;
import com.company.task_manager.repository.AdminAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds the default admin/admin account on first startup, if it doesn't already exist.
// The password is still BCrypt-hashed before storage - "admin" is just the demo seed value.
@Component
public class AdminAccountSeeder implements CommandLineRunner {

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountSeeder(AdminAccountRepository adminAccountRepository, PasswordEncoder passwordEncoder) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminAccountRepository.findByUsername("admin").isEmpty()) {
            adminAccountRepository.save(new AdminAccount("admin", passwordEncoder.encode("admin")));
        }
    }
}
