package com.company.task_manager.controller;

import com.company.task_manager.dto.WorkerRequest;
import com.company.task_manager.dto.WorkerResponse;
import com.company.task_manager.model.Worker;
import com.company.task_manager.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {
    private WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    public Collection<WorkerResponse> getAllWorkers(){
        return workerService.getAllWorkers().stream().map(WorkerResponse::from).toList();
    }


    @GetMapping("/{id}")
    public WorkerResponse getWorkerById(@PathVariable int id){
        return WorkerResponse.from(workerService.findById(id));
    }

    @PostMapping
    public WorkerResponse createWorker(@Valid @RequestBody WorkerRequest request){
        Worker saved = workerService.createWorker(new Worker(request.name()), request.taskIds());
        return WorkerResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorker(@PathVariable int id){
        workerService.deleteWorker(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public WorkerResponse updateWorker(@PathVariable int id, @Valid @RequestBody WorkerRequest request){
        Worker updated = workerService.updateWorker(id, new Worker(request.name()), request.taskIds());
        return WorkerResponse.from(updated);
    }

}
