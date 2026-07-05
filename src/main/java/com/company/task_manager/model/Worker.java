package com.company.task_manager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
public class Worker {

    @Id
    @GeneratedValue
    private int id;

    private String name;

    // mappedBy = "worker" znamená, že vlastníkom vzťahu je pole "worker" v triede Task
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    @JsonIgnore
    private Collection<Task> tasks;

    // Bezparametrický konštruktor - JPA ho vyžaduje
    public Worker() {
        this.tasks = new HashSet<>();
    }

    public Worker(int id) {
        this.id = id;
    }

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

    public Collection<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Collection<Task> tasks) {
        this.tasks = tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
        task.setWorker(this); // dôležité - udržuje konzistenciu vzťahu na oboch stranách
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return id == worker.id && Objects.equals(name, worker.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}