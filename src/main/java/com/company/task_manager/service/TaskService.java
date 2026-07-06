package com.company.task_manager.service;

import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import org.springframework.stereotype.Service;

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

    public Task findById(int id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    public void deleteTask(int id){
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found: " + id);
        }
        taskRepository.deleteById(id);
    }

    public Task createTask(Task task){
        if (task.getWorker() != null) {
            Optional<Worker> workerOpt = workerRepository.findById(task.getWorker().getId());
            task.setWorker(workerOpt.orElse(null));
        }

        if (task.getWorker() != null) {
            assertSameDepartment(task, task.getWorker());
        }

        return taskRepository.save(task);
    }


    public Task updateTask(int id, Task updatedData){
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        existingTask.setName(updatedData.getName()); // odkiaľ zoberieš nové meno?
        existingTask.setStatus(updatedData.getStatus());
        existingTask.setPriority(updatedData.getPriority());
        existingTask.setDueDate(updatedData.getDueDate());
        existingTask.setDepartment(updatedData.getDepartment());

        if (updatedData.getWorker() != null) {
            int workerId = updatedData.getWorker().getId();
            Optional<Worker> workerOpt = workerRepository.findById(workerId);

            if (workerOpt.isPresent()) {
                existingTask.setWorker(workerOpt.get());
            }
        }

        // re-checked unconditionally (not just when the worker changed this request) so a
        // department change alone can't leave a task assigned to a worker in another department
        if (existingTask.getWorker() != null) {
            assertSameDepartment(existingTask, existingTask.getWorker());
        }

        return taskRepository.save(existingTask); // ktorý objekt ukladáš?
    }

    private void assertSameDepartment(Task task, Worker worker) {
        if (task.getDepartment() != worker.getDepartment()) {
            throw new IllegalArgumentException("Task department must match the assigned worker's department");
        }
    }
}
