package com.company.task_manager.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

// Presne jeden Manager na oddelenie - vynucuje WorkerService pri vytváraní
@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends Worker {

    // No-arg constructor required by JPA.
    public Manager() {
        super();
    }

    // Creates a manager with the given name.
    public Manager(String name) {
        super(name);
    }
}
