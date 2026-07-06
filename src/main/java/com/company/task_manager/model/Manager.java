package com.company.task_manager.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

// Presne jeden Manager na oddelenie - vynucuje WorkerService pri vytváraní
@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends Worker {

    public Manager() {
        super();
    }

    public Manager(String name) {
        super(name);
    }
}
