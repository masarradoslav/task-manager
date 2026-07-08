package com.company.task_manager.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class RegularEmployee extends Worker {

    // No-arg constructor required by JPA.
    public RegularEmployee() {
        super();
    }

    // Creates a regular employee with the given name.
    public RegularEmployee(String name) {
        super(name);
    }
}
