package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.Manager;
import com.company.task_manager.model.Worker;
import com.company.task_manager.model.WorkerType;

// Never includes the password hash. workerType is derived from the actual Java subtype.
public record WorkerResponse(int id, String name, Department department, String username, WorkerType workerType) {
    public static WorkerResponse from(Worker worker) {
        WorkerType type = worker instanceof Manager ? WorkerType.MANAGER : WorkerType.EMPLOYEE;
        return new WorkerResponse(worker.getId(), worker.getName(), worker.getDepartment(), worker.getUsername(), type);
    }
}
