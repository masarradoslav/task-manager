package com.company.task_manager.controller;

import com.company.task_manager.model.Worker;
import com.company.task_manager.service.WorkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {
    private WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    public Collection<Worker> getAllWorkers(){
        return workerService.getAllWorkers();
    }


    @GetMapping("/{id}")
    public Optional<Worker> getWorkerById(@PathVariable int id){
        return workerService.findById(id);
    }

    @PostMapping
    public Worker createWorker(@RequestBody Worker worker){
        Worker w = workerService.createWorker(worker);
        return w;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorker(@PathVariable int id){
        workerService.deleteWorker(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Worker updateWorker(@PathVariable int id, @RequestBody Worker updatedData){
        return workerService.updateWorker(id, updatedData);
    }

}
