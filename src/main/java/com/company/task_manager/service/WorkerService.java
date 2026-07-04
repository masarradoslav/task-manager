package com.company.task_manager.service;

import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class WorkerService {
    private WorkerRepository workerRepository;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public Collection<Worker> getAllWorkers(){
        return workerRepository.findAll();
    }

    public Optional<Worker> findById(int id){
        return workerRepository.findById(id);
    }

    public Worker createWorker(Worker worker){
        return workerRepository.save(worker);
    }

    public void deleteWorker(int id){
        workerRepository.deleteById(id);
    }

    public Worker updateWorker(int id, Worker updatedData){
        Optional<Worker> existingWorkerOpt = workerRepository.findById(id);

        if (existingWorkerOpt.isEmpty()) {
            return null;
        }

        Worker existingWorker = existingWorkerOpt.get();
        existingWorker.setName(updatedData.getName()); // odkiaľ zoberieš nové meno?

        return workerRepository.save(existingWorker); // ktorý objekt ukladáš?
    }
}
