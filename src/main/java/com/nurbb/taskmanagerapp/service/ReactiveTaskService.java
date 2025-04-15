package com.nurbb.taskmanagerapp.service;

import com.nurbb.taskmanagerapp.model.entity.Task;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ReactiveTaskService {
    private final Map<UUID, Task> taskStore = new HashMap<>();

    public Mono<Task> createTask(String title, String description){
        return Mono.fromCallable(() -> {
            if (title == null || title.trim().isEmpty()){
                throw new IllegalArgumentException("Task title cannot be empty");
            }
            Task task = Task.builder()
                    .title(title)
                    .description(description)
                    .build();
            taskStore.put(task.getId(),task);
            return task;
        });
    }
    public Flux<Task> getAllTasks(){
        return Flux.fromIterable(taskStore.values())
                .delayElements(Duration.ofSeconds(1));
    }
    public Mono<Task> getTaskById(UUID id){
        return Mono.justOrEmpty(taskStore.get(id));
    }
    public Mono<Task> updateTaskStatus(UUID id, Task.TaskStatus newStatus){
     return Mono.justOrEmpty(taskStore.get(id))
             .switchIfEmpty(Mono.error(new IllegalArgumentException("Task not found with id: " + id)))
             .map(task -> {
                 Task updatedTask = task.updateStatus(newStatus);
                 taskStore.put(id, updatedTask);
                 return updatedTask;
             });
    }
    public Mono<Void> deleteTask(UUID id){
        return Mono.fromRunnable(() -> taskStore.remove(id));
    }
}
