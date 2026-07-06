package com.company.task_manager.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class RegularEmployee extends Worker {

    public RegularEmployee() {
        super();
    }

    public RegularEmployee(String name) {
        super(name);
    }
}
