package com.company.task_manager.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import org.hibernate.annotations.DiscriminatorOptions;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Version;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "worker_type")

@DiscriminatorOptions(force = true)
public class Worker {

    @Id
    @GeneratedValue
    private int id;

    private String name;

    @Column(unique = true)
    private String username;

    // BCrypt hash
    private String password;

    @Enumerated(EnumType.STRING)
    private Department department;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    private Collection<Task> tasks;

    // optimistic-locking guard
    @Version
    private long version;

    // no-arg constructor required by JPA
    public Worker() {
        this.tasks = new HashSet<>();
    }

    // creates a placeholder with only the id set, used to reference an existing worker before
    // the service layer resolves it to a real entity.
    public Worker(int id) {
        this.id = id;
    }

    // creates a worker with just a name (department/username/password set separately).
    public Worker(String name) {
        this.name = name;
        this.tasks = new HashSet<>();
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Collection<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Collection<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
        task.setWorker(this);
    }

    // no setter - Hibernate owns this field, same as id
    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return id == worker.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
