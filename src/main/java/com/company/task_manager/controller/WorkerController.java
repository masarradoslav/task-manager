package com.company.task_manager.controller;

import com.company.task_manager.dto.PageResponse;
import com.company.task_manager.dto.WorkerRequest;
import com.company.task_manager.dto.WorkerResponse;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Worker;
import com.company.task_manager.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {
    private WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    public PageResponse<WorkerResponse> getAllWorkers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Department department
    ){
        Page<Worker> result = workerService.getWorkers(page, size, department);
        return PageResponse.from(result.map(WorkerResponse::from));
    }


    @GetMapping("/{id}")
    public WorkerResponse getWorkerById(@PathVariable int id){
        return WorkerResponse.from(workerService.findById(id));
    }

    @PostMapping
    public WorkerResponse createWorker(@Valid @RequestBody WorkerRequest request){
        Worker saved = workerService.createWorker(toEntity(request), request.taskIds());
        return WorkerResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorker(@PathVariable int id){
        workerService.deleteWorker(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public WorkerResponse updateWorker(@PathVariable int id, @Valid @RequestBody WorkerRequest request){
        Worker updated = workerService.updateWorker(id, toEntity(request), request.taskIds());
        return WorkerResponse.from(updated);
    }

    private Worker toEntity(WorkerRequest request) {
        Worker worker = new Worker(request.name());
        worker.setDepartment(request.department());
        return worker;
    }

}
