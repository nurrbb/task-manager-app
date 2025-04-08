package com.nurbb.taskmanagerapp.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Task {

   private UUID id;
   private String title;
   private String description;
   private TaskStatus status;
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;

   public enum TaskStatus {
       TODO,
       IN_PROGRESS,
       DONE
   }
   public UUID getId(){
       return id;
   }

   public void setId(UUID id){
       this.id = id;
   }

    public Task(UUID id, String title, String description, TaskStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }



    public Task() {
    }
}
