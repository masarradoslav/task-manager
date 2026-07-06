package com.company.task_manager.repository;

import com.company.task_manager.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Integer>, JpaSpecificationExecutor<Worker> {

    Optional<Worker> findByUsername(String username);
}
