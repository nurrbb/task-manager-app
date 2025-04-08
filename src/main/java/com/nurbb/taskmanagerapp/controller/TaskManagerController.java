package com.nurbb.taskmanagerapp.controller;

import com.nurbb.taskmanagerapp.model.dto.TaskCreateRequest;
import com.nurbb.taskmanagerapp.model.entity.Task;
import com.nurbb.taskmanagerapp.service.TaskManagerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskManagerController {

    private TaskManagerService taskManagerService;

    public TaskManagerController(TaskManagerService taskManagerService) {
        this.taskManagerService = taskManagerService;
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Task createTask(@RequestBody  TaskCreateRequest taskCreateRequest) {
        System.out.println("taskCreateRequest: " + taskCreateRequest);
        return taskManagerService.createTask(taskCreateRequest);
    }
}
