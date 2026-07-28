package com.company.task_manager.model;

// used only on worker creation to pick Worker subclass
// ignored on update,worker subtype can not change after creation
public enum WorkerType {
    MANAGER,
    EMPLOYEE
}
