package com.company.task_manager.repository;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {
    boolean existsByDepartment(Department department);
}
