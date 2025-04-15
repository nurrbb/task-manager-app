package com.nurbb.taskmanagerapp.model.dto.response;

public record TaskStatistics(
        long total,
        long pending,
        long inProgress,
        long blocked,
        long completed
) {}
