package com.nurbb.taskmanagerapp.controller;

import com.nurbb.taskmanagerapp.model.dto.request.StatusUpdateRequest;
import com.nurbb.taskmanagerapp.model.dto.request.TaskRequest;
import com.nurbb.taskmanagerapp.model.entity.Task;
import com.nurbb.taskmanagerapp.model.exception.TaskNotFoundException;
import com.nurbb.taskmanagerapp.service.ReactiveTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/reactive/tasks")
public class ReactiveTaskController {

    private final ReactiveTaskService taskService;

    @Autowired
    public ReactiveTaskController(ReactiveTaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Task> createTask(@RequestBody TaskRequest request) {
        return taskService.createTask(request.getTitle(),request.getDescription());
    }

    @GetMapping("/{id}")
    public Mono<Task> getTaskById(@PathVariable UUID id) {
        return taskService.getTaskById(id)
                .switchIfEmpty(Mono.error(new TaskNotFoundException(id)));
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PatchMapping("/{id}/status")
    public  Mono<Task> updateTaskStatus(
            @PathVariable UUID id,
            @RequestBody StatusUpdateRequest request){

        return taskService.updateTaskStatus(id,request.getStatus())
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.error(new TaskNotFoundException(id)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteTask(@PathVariable UUID id) {
        return taskService.deleteTask(id);
    }
}
