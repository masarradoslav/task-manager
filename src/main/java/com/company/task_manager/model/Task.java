package com.company.task_manager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Task {

    @Id
    @GeneratedValue
    private int id;

    private String name;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    // Lokálny dátum/čas bez časovej zóny - termín splnenia úlohy
    @FutureOrPresent
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private Department department;

    // Toto je "vlastník" vzťahu - v tabuľke task bude stĺpec worker_id
    @ManyToOne
    private Worker worker;

    // Bezparametrický konštruktor - JPA ho vyžaduje
    // No-arg constructor required by JPA.
    public Task() {
    }

    // Creates a task with default TODO status and MEDIUM priority.
    public Task(String name) {
        this.name = name;
        this.status = TaskStatus.TODO;
        this.priority = TaskPriority.MEDIUM;
    }

    // Creates a task already assigned to the given worker.
    public Task(Worker worker, String name) {
        this.worker = worker;
        this.name = name;
    }

    // Returns the task's id.
    public int getId() {
        return id;
    }

    // Returns the task's name.
    public String getName() {
        return name;
    }

    // Sets the task's name.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the assigned worker, or null if unassigned.
    public Worker getWorker() {
        return worker;
    }

    // Sets the assigned worker.
    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    // Returns the task's status.
    public TaskStatus getStatus() {
        return status;
    }

    // Sets the task's status.
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    // Returns the task's priority.
    public TaskPriority getPriority() {
        return priority;
    }

    // Sets the task's priority.
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    // Returns the task's due date.
    public LocalDateTime getDueDate() {
        return dueDate;
    }

    // Sets the task's due date.
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    // Returns the task's department.
    public Department getDepartment() {
        return department;
    }

    // Sets the task's department.
    public void setDepartment(Department department) {
        this.department = department;
    }

    // Equal when id and name both match.
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name);
    }

    // Hash based on id and name, consistent with equals.
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}