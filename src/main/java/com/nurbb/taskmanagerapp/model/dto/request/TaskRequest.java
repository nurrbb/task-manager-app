package com.nurbb.taskmanagerapp.model.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {
    private String title;
    private String description;

}
