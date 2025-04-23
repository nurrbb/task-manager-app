package com.nurbb.taskmanagerapp.controller;


import com.nurbb.taskmanagerapp.model.dto.request.PriorityUpdateRequest;
import com.nurbb.taskmanagerapp.model.dto.request.StatusUpdateRequest;
import com.nurbb.taskmanagerapp.model.dto.request.TaskRequest;
import com.nurbb.taskmanagerapp.model.dto.response.TaskResponseDTO;
import com.nurbb.taskmanagerapp.model.dto.response.TaskStatistics;
import com.nurbb.taskmanagerapp.model.entity.Task;
import com.nurbb.taskmanagerapp.service.TaskService;
import com.nurbb.taskmanagerapp.service.TaskStatusNotAvailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//İstemciden HTTP isteği gelir (POST,GET,PUT)
//DispatcherServlet URL ile eşleşen yöntem bulur
//Metot iş mantığını barındıran service sınıfına çağrı yapar
//Service den gelen HTTP cevabı responseEntity olarak döndürülür

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    //Constructor - based dependency injection örneğidir.
    // Task service sınıfı controllera inject edilmiştir.
    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)

    //request içeriği TaskRequest modeline bağlanır.
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody TaskRequest request){
        TaskResponseDTO newTask = taskService.createTask(request.getTitle(),request.getDescription());
        return new ResponseEntity<>(newTask, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTaks() {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable UUID id) {
        TaskResponseDTO task = taskService.getTaskById(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable UUID id,
            @RequestBody StatusUpdateRequest request) {
        try {
            Task updatedTask = taskService.updateTaskStatus(id, request.getStatus());
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (TaskStatusNotAvailableException ex) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public  ResponseEntity<List<TaskResponseDTO>> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<TaskResponseDTO> findTaskByTitle(@PathVariable String title) {
        return taskService.findTaskByTitle(title)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/statistics")
    public ResponseEntity<TaskStatistics> getTaskStatistics() {
        return ResponseEntity.ok(taskService.getTaskStatistics());
    }

    @GetMapping("/report")
    public ResponseEntity<String> generateReport() {
        return ResponseEntity.ok(taskService.generateTaskReport());
    }

    @GetMapping("/async")
    public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getTasksAsync() {
        return taskService.getTasksAsync()
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/group-by-status")
    public ResponseEntity<Map<Task.TaskStatus, List<TaskResponseDTO>>> groupTasksByStatus() {
        return ResponseEntity.ok(taskService.groupTasksByStatus());
    }

    @PostMapping("/analyze-durations")
    public ResponseEntity<List<String>> analyzeTaskDurations(@RequestBody List<TaskService.TaskDuration> duration) {
        return ResponseEntity.ok(taskService.analyzeTaskDuration(duration));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<String> getTaskSummary(@PathVariable UUID id) {
        try{
            return ResponseEntity.ok(taskService.getTaskSummary(id));
        }catch (IllegalArgumentException e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/priority-object")
    public ResponseEntity<Map<String, Object>> getTaskPriorityObject(@PathVariable UUID id) {
        try {
            Task.Priority priority = taskService.getTaskPriorityObject(id);

            // Create a map to represent the priority since we can't directly serialize the interface
            Map<String, Object> response = Map.of(
                    "label", priority.getLabel(),
                    "value", priority.getValue(),
                    "type", priority.getClass().getSimpleName()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/{id}/priority")
    public ResponseEntity<Task> updateTaskPriority(
            @PathVariable UUID id,
            @RequestBody PriorityUpdateRequest request) {
        try {
            // Convert string or int to TaskPriority object based on request
            Task.Priority priority = switch(request.value()) {
                case 1 -> new Task.LowPriority();
                case 2 -> new Task.MediumPriority();
                case 3 -> new Task.HighPriority();
                default -> throw new IllegalArgumentException("Invalid priority value");
            };

            Task updatedTask = taskService.updateTaskPriority(id, priority);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/status-statistics")
    public ResponseEntity<Map<String,Object>> getTaskStatusStatistics() {
        return ResponseEntity.ok(taskService.getTaskStatusStatistics());
    }

}
