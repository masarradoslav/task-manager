package com.company.task_manager.security;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.Manager;
import com.company.task_manager.repository.ManagerRepository;
import com.company.task_manager.repository.WorkerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

// Seeds a default manager account per department (it/it, hr/hr, acc/acc) for manual testing.
// Like AdminAccountSeeder it never overwrites anything: a department that already has a manager,
// or a default username that is already taken, is simply skipped.
@Component
public class DefaultManagerSeeder implements CommandLineRunner {

    private static final Map<Department, String[]> DEFAULT_MANAGERS = Map.of(
            Department.IT, new String[]{"IT Manager", "it", "it"},
            Department.HR, new String[]{"HR Manager", "hr", "hr"},
            Department.ACCOUNTING, new String[]{"Accounting Manager", "acc", "acc"}
    );

    private final ManagerRepository managerRepository;
    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultManagerSeeder(ManagerRepository managerRepository, WorkerRepository workerRepository,
                                 PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.workerRepository = workerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Creates each department's default manager at startup if the slot and username are free.
    @Override
    public void run(String... args) {
        DEFAULT_MANAGERS.forEach((department, nameAndCredentials) -> {
            String name = nameAndCredentials[0];
            String username = nameAndCredentials[1];
            String password = nameAndCredentials[2];
            if (managerRepository.existsByDepartment(department)) return;
            if (workerRepository.findByUsername(username).isPresent()) return;

            Manager manager = new Manager(name);
            manager.setDepartment(department);
            manager.setUsername(username);
            manager.setPassword(passwordEncoder.encode(password));
            workerRepository.save(manager);
        });
    }
}
