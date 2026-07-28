package com.company.task_manager.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends Worker {

    // no-arg constructor required by JPA
    public Manager() {
        super();
    }

    // creates a manager with the given name.
    public Manager(String name) {
        super(name);
    }
}
