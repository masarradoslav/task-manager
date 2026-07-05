package com.company.task_manager.model;

import jakarta.persistence.*;

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

    // Toto je "vlastník" vzťahu - v tabuľke task bude stĺpec worker_id
    @ManyToOne
    private Worker worker;

    // Bezparametrický konštruktor - JPA ho vyžaduje
    public Task() {
    }

    public Task(String name) {
        this.name = name;
        this.status = TaskStatus.TODO;
        this.priority = TaskPriority.MEDIUM;
    }

    public Task(Worker worker, String name) {
        this.worker = worker;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}