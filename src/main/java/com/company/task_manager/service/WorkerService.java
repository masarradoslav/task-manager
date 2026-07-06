package com.company.task_manager.service;

import com.company.task_manager.exception.ResourceNotFoundException;
import com.company.task_manager.model.Task;
import com.company.task_manager.model.Worker;
import com.company.task_manager.repository.TaskRepository;
import com.company.task_manager.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                assertSameDepartment(savedWorker, task);
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

    @Transactional
    public Worker updateWorker(int id, Worker updatedData, List<Integer> taskIds){
        Worker existingWorker = workerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found: " + id));

        existingWorker.setName(updatedData.getName()); // odkiaľ zoberieš nové meno?
        existingWorker.setDepartment(updatedData.getDepartment());

        if (taskIds != null) {
            Set<Integer> desiredIds = new HashSet<>(taskIds);

            for (Task task : new ArrayList<>(existingWorker.getTasks())) {
                if (!desiredIds.contains(task.getId())) {
                    existingWorker.getTasks().remove(task);
                    task.setWorker(null);
                    taskRepository.save(task);
                }
            }

            for (Integer taskId : desiredIds) {
                boolean alreadyAssigned = existingWorker.getTasks().stream()
                        .anyMatch(task -> task.getId() == taskId);
                if (!alreadyAssigned) {
                    Task task = taskRepository.findById(taskId)
                            .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
                    assertSameDepartment(existingWorker, task);
                    existingWorker.addTask(task);
                    taskRepository.save(task);
                }
            }
        }

        // re-checked unconditionally (covers tasks kept from before this update, in case the
        // worker's department just changed) so no task can end up in another department's worker
        for (Task task : existingWorker.getTasks()) {
            assertSameDepartment(existingWorker, task);
        }

        return workerRepository.save(existingWorker); // ktorý objekt ukladáš?
    }

    private void assertSameDepartment(Worker worker, Task task) {
        if (worker.getDepartment() != task.getDepartment()) {
            throw new IllegalArgumentException("Worker department must match the assigned task's department");
        }
    }
}
