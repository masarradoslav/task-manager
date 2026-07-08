package com.company.task_manager.controller;

import com.company.task_manager.dto.PageResponse;
import com.company.task_manager.dto.WorkerRequest;
import com.company.task_manager.dto.WorkerResponse;
import com.company.task_manager.model.Department;
import com.company.task_manager.model.Worker;
import com.company.task_manager.security.AppUserPrincipal;
import com.company.task_manager.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {
    private WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    // GET /workers - paginated worker list with optional department filter.
    @GetMapping
    public PageResponse<WorkerResponse> getAllWorkers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Department department,
            @AuthenticationPrincipal AppUserPrincipal principal
    ){
        Page<Worker> result = workerService.getWorkers(page, size, department, principal);
        return PageResponse.from(result.map(WorkerResponse::from));
    }


    // GET /workers/{id} - one worker by id.
    @GetMapping("/{id}")
    public WorkerResponse getWorkerById(@PathVariable int id, @AuthenticationPrincipal AppUserPrincipal principal){
        return WorkerResponse.from(workerService.findById(id, principal));
    }

    // POST /workers - create a worker (Manager or RegularEmployee).
    @PostMapping
    public WorkerResponse createWorker(@Valid @RequestBody WorkerRequest request,
                                        @AuthenticationPrincipal AppUserPrincipal principal){
        Worker saved = workerService.createWorker(request.name(), request.department(), request.username(),
                request.password(), request.workerType(), request.taskIds(), principal);
        return WorkerResponse.from(saved);
    }

    // DELETE /workers/{id} - remove a worker.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorker(@PathVariable int id, @AuthenticationPrincipal AppUserPrincipal principal){
        workerService.deleteWorker(id, principal);
        return ResponseEntity.noContent().build();
    }

    // PUT /workers/{id} - edit a worker (restricted to name/username/password when self-editing).
    @PutMapping("/{id}")
    public WorkerResponse updateWorker(@PathVariable int id, @Valid @RequestBody WorkerRequest request,
                                        @AuthenticationPrincipal AppUserPrincipal principal){
        Worker updated = workerService.updateWorker(id, request.name(), request.department(), request.username(),
                request.password(), request.taskIds(), principal);
        return WorkerResponse.from(updated);
    }

}
