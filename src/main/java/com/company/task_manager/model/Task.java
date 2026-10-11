package com.company.task_manager.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
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

    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private Department department;

    @ManyToOne
    private Worker worker;

    private long version;

    // comments are deleted along with the task they belong to
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private Collection<Comment> comments;

    // no-arg constructor required by JPA
    public Task() {
        this.comments = new HashSet<>();
    }

    // Creates a task with default TODO status and MEDIUM priority
    public Task(String name) {
        this.name = name;
        this.status = TaskStatus.TODO;
        this.priority = TaskPriority.MEDIUM;
        this.comments = new HashSet<>();
    }

    // creates a task already assigned to the given worker
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    // no setter - Hibernate owns this field, same as id
    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}