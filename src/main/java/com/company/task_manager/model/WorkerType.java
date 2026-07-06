package com.company.task_manager.model;

// Selector used only on worker creation to pick which Worker subclass to instantiate -
// ignored on update, since a worker's concrete subtype can't change after creation.
public enum WorkerType {
    MANAGER,
    EMPLOYEE
}
