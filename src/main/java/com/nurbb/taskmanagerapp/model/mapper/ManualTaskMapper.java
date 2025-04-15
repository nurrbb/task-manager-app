package com.nurbb.taskmanagerapp.model.mapper;

import com.nurbb.taskmanagerapp.model.dto.response.TaskResponseDTO;
import com.nurbb.taskmanagerapp.model.entity.Task;

public class ManualTaskMapper {

    private ManualTaskMapper() {}

    public static TaskResponseDTO toDTO(Task task){
        if(task == null){
            return null;
        }
        return  new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getPriority()
        );
    }
}
