package com.nurbb.taskmanagerapp.model.mapper;

import com.nurbb.taskmanagerapp.model.dto.response.TaskResponseDTO;
import com.nurbb.taskmanagerapp.model.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskResponseMapper {

    TaskResponseMapper INSTANCE = Mappers.getMapper(TaskResponseMapper.class);

    TaskResponseDTO toDTO(Task task);
    List<TaskResponseDTO> toDTOList(List<Task> tasks);
}
