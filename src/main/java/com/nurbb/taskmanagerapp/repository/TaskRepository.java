package com.nurbb.taskmanagerapp.repository;

import com.nurbb.taskmanagerapp.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//Veri katmanı olarak işaretliyoruz DI için tanımlama yapılır.
@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {


    List<Task> findByStatus(Task.TaskStatus status);

    List<Task> findByTitleContainingIgnoreCase(String title);

    Optional<Task> findFirstByOrderByCreatedAtDesc();

    long countByStatus(Task.TaskStatus status);

    List<Task> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.priorityValue= : value ORDER BY t.createdAt DESC ")
    List<Task> findTasksByPriorityValue(@Param("Value") int priorityValue);

    @Query("SELECT t FROM Task t WHERE t.createdAt < : date AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("date") LocalDateTime date);

    @Query(value = """
            SELECT * FROM tasks
            WHERE status != 'COMPLETED'
            AND priority_value >= :minPriority
            ORDER BY priority_value DESC, created_at ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Task> findPriorityTasksToComplete(
            @Param("minPriority") int minPriority,
            @Param("limit") int limit);

    @Query(value = """
            SELECT 
                status,
                COUNT(*) as task_count,
                MIN(created_at) as oldest_task,
                MAX(created_at) as newest_task
            FROM tasks
            GROUP BY status
            """, nativeQuery = true)
    List<Object[]> getTaskStatusStatistics();

}
