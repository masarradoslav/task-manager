package com.company.task_manager.dto;

import com.company.task_manager.model.Department;
import com.company.task_manager.model.Worker;

// Neobsahuje kolekciu tasks - nahrádza pôvodné @JsonIgnore na Worker.tasks
public record WorkerResponse(int id, String name, Department department) {
    public static WorkerResponse from(Worker worker) {
        return new WorkerResponse(worker.getId(), worker.getName(), worker.getDepartment());
    }
}
