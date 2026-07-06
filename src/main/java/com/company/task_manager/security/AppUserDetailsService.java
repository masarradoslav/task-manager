package com.company.task_manager.security;

import com.company.task_manager.repository.AdminAccountRepository;
import com.company.task_manager.repository.WorkerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AdminAccountRepository adminAccountRepository;
    private final WorkerRepository workerRepository;

    public AppUserDetailsService(AdminAccountRepository adminAccountRepository, WorkerRepository workerRepository) {
        this.adminAccountRepository = adminAccountRepository;
        this.workerRepository = workerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminAccountRepository.findByUsername(username)
                .<UserDetails>map(AppUserPrincipal::forAdmin)
                .or(() -> workerRepository.findByUsername(username).map(AppUserPrincipal::forWorker))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
