package com.nurbb.taskmanagerapp.model.exception;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(UUID id) {
        super("Task with id " + id + " not found");
    }
}
