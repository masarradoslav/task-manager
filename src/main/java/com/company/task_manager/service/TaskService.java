package com.company.task_manager.service;

import com.company.task_manager.model.Task;
import com.company.task_manager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class TaskService {
    private TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Collection<Task> getAllTasks(){
        return taskRepository.findAll();
    }

    public Optional<Task> findById(int id){
        return taskRepository.findById(id);
    }

    public void createTask(Task task){
        taskRepository.save(task);
    }

    public void deleteTask(int id){
        taskRepository.deleteById(id);
    }
}
