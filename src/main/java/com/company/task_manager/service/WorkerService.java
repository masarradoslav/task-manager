package com.company.task_manager.service;

import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class WorkerService {
    private WorkerRepository workerRepository;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public Collection<Worker> getAllWorkers(){
        return workerRepository.findAll();
    }

    public Worker findById(int id){
        return workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));
    }

    public Worker createWorker(Worker worker){
        return workerRepository.save(worker);
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
