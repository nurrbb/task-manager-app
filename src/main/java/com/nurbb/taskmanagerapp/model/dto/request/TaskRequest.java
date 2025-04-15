package com.nurbb.taskmanagerapp.model.dto.request;

import lombok.Getter;

@Getter
public class TaskRequest {
    private String title;
    private String description;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
