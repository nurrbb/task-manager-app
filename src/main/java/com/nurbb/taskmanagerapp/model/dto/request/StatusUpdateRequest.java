package com.nurbb.taskmanagerapp.model.dto.request;

import com.nurbb.taskmanagerapp.model.entity.Task;

public class StatusUpdateRequest {

   private Task.TaskStatus status;

   public Task.TaskStatus getStatus() {
       return status;
   }
   public void setStatus(Task.TaskStatus status) {
       this.status = status;
   }
}
