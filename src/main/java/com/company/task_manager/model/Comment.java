package com.company.task_manager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Comment {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    private String content;

    private LocalDateTime dateCreated;

    // stays null until the comment is edited
    private LocalDateTime dateLastEdit;

    @ManyToOne(optional = false)
    private Task task;

    // the worker who wrote the comment; always a Manager/Employee, never an Admin
    @ManyToOne(optional = false)
    private Worker worker;

    // no-arg constructor required by JPA
    public Comment() {
    }

    // creates a comment with just name/content (task/worker/dateCreated set separately)
    public Comment(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateLastEdit() {
        return dateLastEdit;
    }

    public void setDateLastEdit(LocalDateTime dateLastEdit) {
        this.dateLastEdit = dateLastEdit;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return id == comment.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
