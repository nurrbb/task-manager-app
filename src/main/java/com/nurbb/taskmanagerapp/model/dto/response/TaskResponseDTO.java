package com.nurbb.taskmanagerapp.model.dto.response;

import com.nurbb.taskmanagerapp.model.entity.Task;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        Task.TaskStatus status,
        LocalDateTime createdAt,
        Task.Priority priority
) {}
