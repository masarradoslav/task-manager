package com.company.task_manager.service;

import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
public class WorkerService {
    private WorkerRepository workerRepository;
    private TaskRepository taskRepository;

    public WorkerService(WorkerRepository workerRepository, TaskRepository taskRepository) {
        this.workerRepository = workerRepository;
        this.taskRepository = taskRepository;
    }

    public Collection<Worker> getAllWorkers(){
        return workerRepository.findAll();
    }

    public Worker findById(int id){
        return workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));
    }

    @Transactional
    public Worker createWorker(Worker worker, List<Integer> taskIds){
        Worker savedWorker = workerRepository.save(worker);

        if (taskIds != null) {
            for (Integer taskId : taskIds) {
                Task task = taskRepository.findById(taskId)
                        .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
                savedWorker.addTask(task);
                taskRepository.save(task);
            }
        }

        return savedWorker;
    }

    public void deleteWorker(int id){
        if (!workerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Worker not found: " + id);
        }
        workerRepository.deleteById(id);
    }

    public Worker updateWorker(int id, Worker updatedData){
        Worker existingWorker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));

        existingWorker.setName(updatedData.getName()); // odkiaľ zoberieš nové meno?

        return workerRepository.save(existingWorker); // ktorý objekt ukladáš?
    }
}
