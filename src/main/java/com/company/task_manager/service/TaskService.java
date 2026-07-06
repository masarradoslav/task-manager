package com.company.task_manager.service;

import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

@Service
public class TaskService {
    private TaskRepository taskRepository;
    private WorkerRepository workerRepository;


    public TaskService(TaskRepository taskRepository, WorkerRepository workerRepository) {
        this.taskRepository = taskRepository;
        this.workerRepository = workerRepository;
    }

    public Collection<Task> getAllTasks(){
        return taskRepository.findAll();
    }

    public Optional<Task> findById(int id){
        return taskRepository.findById(id);
    }

    public void deleteTask(int id){
        taskRepository.deleteById(id);
    }

    public Task createTask(Task task){
        validateDueDate(task.getDueDate());

        if (task.getWorker() != null) {
            Optional<Worker> workerOpt = workerRepository.findById(task.getWorker().getId());
            task.setWorker(workerOpt.orElse(null));
        }

        return taskRepository.save(task);
    }


    public Task updateTask(int id, Task updatedData){
        Optional<Task> existingTaskOpt = taskRepository.findById(id);

        if (existingTaskOpt.isEmpty()) {
            return null;
        }

        Task existingTask = existingTaskOpt.get();
        existingTask.setName(updatedData.getName()); // odkiaľ zoberieš nové meno?

        if (updatedData.getWorker() != null) {
            int workerId = updatedData.getWorker().getId();
            Optional<Worker> workerOpt = workerRepository.findById(workerId);

            if (workerOpt.isPresent()) {
                existingTask.setWorker(workerOpt.get());
            }
        }

        if (updatedData.getStatus() != null) {
            existingTask.setStatus(updatedData.getStatus());
        }

        if (updatedData.getPriority() != null) {
            existingTask.setPriority(updatedData.getPriority());
        }

        if (updatedData.getDueDate() != null) {
            validateDueDate(updatedData.getDueDate());
            existingTask.setDueDate(updatedData.getDueDate());
        }

        return taskRepository.save(existingTask); // ktorý objekt ukladáš?
    }

    private void validateDueDate(LocalDateTime dueDate) {
        if (dueDate != null && !dueDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }
    }
}
