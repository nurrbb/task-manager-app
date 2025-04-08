package com.nurbb.taskmanagerapp.service;

import com.nurbb.taskmanagerapp.model.dto.TaskCreateRequest;
import com.nurbb.taskmanagerapp.model.entity.Task;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskManagerService {
    Map<String, Task> tasks = new HashMap<>();

    public Task createTask(TaskCreateRequest taskCreateRequest ) {
        Task task = new Task();
        UUID id = UUID.randomUUID();
        task.setId(id);
        task.setTitle(taskCreateRequest.getTitle());
        task.setDescription(taskCreateRequest.getDescription());
        task.setStatus(Task.TaskStatus.TODO);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        tasks.put(id.toString(), task);
        return task;
    }

    public Map<String,Task>  getTasks(){
        return tasks;
    }

    public Task getTask(String id){
        return tasks.get(id);
    }

}
