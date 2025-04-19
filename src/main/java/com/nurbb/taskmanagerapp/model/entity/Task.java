package com.nurbb.taskmanagerapp.model.entity;

import jakarta.persistence.*;
import  lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Task {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @With
    private TaskStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "priority_value")
    private int priorityValue;

    @Column(name = "priority_label")
    private String priorityLabel;

    public Task.Priority getPriority() {
        return switch (status) {
            case PENDING, COMPLETED -> new LowPriority();
            case IN_PROGRESS -> new MediumPriority();
            case BLOCKED -> new HighPriority();
        };
    }

    // JPA Lifecycle anotasyonu: Veritabanı işlemleri gerçekleşmeden hemen önce ya da sonra otomatik olarak çalıştırılır
    // Boş olan alanları dolduruluyor
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();

        updatePriorityFields();
    }

    //JPA Lifecycle anotasyonu.
    // Bir nesne  veritabanında güncelleme yapıldıktan sonra otomatik olarak tetiklenir
    @PostUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private void updatePriorityFields() {
        Priority priority = switch (status) {
            case PENDING, COMPLETED -> new LowPriority();
            case IN_PROGRESS -> new MediumPriority();
            case BLOCKED -> new HighPriority();
        };

        this.priorityValue = priority.getValue();
        this.priorityLabel = priority.getLabel();
    }

    public static class TaskBuilder {
        private UUID id = UUID.randomUUID();
        private TaskStatus status = TaskStatus.PENDING;
        private LocalDateTime createdAt = LocalDateTime.now();
    }

    // Task'ın kopyası oluşturulur status alanını newStatus ile değiştirilir
    // Nesneler doğrudan değştirilmez, her değişiklikte yeni bir nesne oluşturulur.
    // @WithXXX:  Sadece bir alanı değiştirip geri kalan her şeyi aynı bırakarak yeni bir nesne üreten Lombok'un metodu.
    public Task updateStatus(TaskStatus newStatus) {
        Task updated = this.withStatus(newStatus);
        updated.updatedAt = LocalDateTime.now();
        updated.updatePriorityFields();
        return updated;
    }

    public enum TaskStatus {
        PENDING, IN_PROGRESS, BLOCKED, COMPLETED
    }

    //Sealed interface: Kontrollü kalıtım için permit ile belirtilen sınıfların yalnızca sealed interface i implement etmesine izin verilir.
    public sealed interface Priority permits LowPriority, MediumPriority, HighPriority {
        String getLabel();
        int getValue();
    }

    public enum TaskPriority {
        LOW( 1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High");

        private final int value;
        private final String label;

        TaskPriority(int value, String label) {
            this.value = value;
            this.label = label;
        }
    }

    public static final class LowPriority implements Priority {
        @Override
        public String   getLabel() {
            return TaskPriority.LOW.label;
        }

        @Override
        public int getValue() {
            return TaskPriority.LOW.value;
        }
    }

    public static final class MediumPriority implements Priority {
        @Override
        public String getLabel() {
            return TaskPriority.MEDIUM.label;
        }

        @Override
        public int getValue() {
            return TaskPriority.MEDIUM.value;
        }
    }

    public static final class HighPriority implements Priority {
        @Override
        public String getLabel() {
            return TaskPriority.HIGH.label;
        }

        @Override
        public int getValue() {
            return TaskPriority.HIGH.value;
        }
    }
}
