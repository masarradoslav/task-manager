package com.company.task_manager.repository;

import com.company.task_manager.model.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminAccountRepository extends JpaRepository<AdminAccount, Integer> {
    Optional<AdminAccount> findByUsername(String username);
}
