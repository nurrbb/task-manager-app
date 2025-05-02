package com.nurbb.taskmanagerapp.service;

import com.nurbb.taskmanagerapp.model.dto.response.TaskResponseDTO;
import com.nurbb.taskmanagerapp.model.dto.response.TaskStatistics;
import com.nurbb.taskmanagerapp.model.entity.Task;
import com.nurbb.taskmanagerapp.model.exception.TaskStatusNotAvailableException;
import com.nurbb.taskmanagerapp.model.mapper.ManualTaskMapper;
import com.nurbb.taskmanagerapp.model.mapper.TaskResponseMapper;
import com.nurbb.taskmanagerapp.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

//CRUD

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final List<Consumer<Task>> taskCreationListeners = new ArrayList<>();
    private final List<Consumer<Task>> taskCompletionListeners = new ArrayList<>();

// OBSERVER PATTERN:
// Task ile ilgili bir şey yapıldığında o olayın sonucu olarak başka şeylerin sistem içinde tetiklenmesini sağlamak
// geleneksel olarak her işlem sonrası manuel olarak diğer servisleri çağırma ihtiyacını ortadan kaldırır (event-driven)
// exp: Task completed >>loglama yapılması ve kullanıcıya notif gönderilmesi Task Service bunları bilmez
// onun yerine service'e Consumer <Task> tipinde fonk eklenir. Görev tamamlanınca bu fonk otomatik tetiklenir


    // İşlemin tek bir bütün olarak yürütülmesini sağlar.
    // Hata olursa rollback mekanizması çalışır tüm değişiklikler geri alınır.
    // Veri bütünlüğü korunur.
    @Transactional
    public TaskResponseDTO createTask(String title,String description) {
        validateTaskInput(title,description); //Boş başlık oluşturmaya karşı kontrol
        Task task = Task.builder() //yeni görev oluşturma
                .title(title)
                .description(description)
                .build();
        Task savedTask = taskRepository.save(task);

        taskCreationListeners.forEach(listener -> listener.accept(savedTask));

        return ManualTaskMapper.toDTO(savedTask); //Dışarıya gönderilmeden önce Dto ya çevirilir
    }

    public List<TaskResponseDTO> getAllTasks() {
        return TaskResponseMapper.INSTANCE.toDTOList(taskRepository.findAll());
        //Mapper ile repo’dan gelen tüm görevler DTO’ya çevrilir ve response olarak döner
    }


    public TaskResponseDTO getTaskById(UUID id) {
        return taskRepository.findById(id)
                .map(TaskResponseMapper.INSTANCE::toDTO)
                .orElse(null);
    }

    @Transactional
    public Task updateTaskStatus(UUID id, Task.TaskStatus newStatus) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));

         if(task.getStatus() == Task.TaskStatus.BLOCKED){
             throw new TaskStatusNotAvailableException("Task is not found with id: " + id);
         }
         Task updatedTask = task.updateStatus(newStatus);
         Task savedTask = taskRepository.save(updatedTask);

         if(newStatus == Task.TaskStatus.COMPLETED){
             taskCompletionListeners.forEach(listener -> listener.accept(savedTask));
         }
         return savedTask;
    }
    @Transactional
    public void deleteTaskById(UUID id) {
        taskRepository.deleteById(id);
    }

    //Empty title ve descp önlenmesi için

    private void validateTaskInput(String title, String description) {
        if(title == null || title.trim().isEmpty()){
            throw new IllegalArgumentException("Task title cannot empty.");
        }
        if(description == null ){
            throw new IllegalArgumentException("Task description cannot empty.");
        }
    }
    public List<TaskResponseDTO> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(TaskResponseMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
    } //birden fazla olabileceği için dönüş tipi list buduktan sonra da collect ediyoruz

    public Optional<TaskResponseDTO> findTaskByTitle(String title) {
        return taskRepository.findByTitleContainingIgnoreCase(title).stream()
                .findFirst()
                .map(TaskResponseMapper.INSTANCE::toDTO);
    }
     public List<TaskResponseDTO> getTasksByPriority(int priorityValue) {
        return taskRepository.findTasksByPriorityValue(priorityValue).stream()
                .map(TaskResponseMapper.INSTANCE::toDTO)
                .collect(Collectors.toList());
     }
     @Transactional
    public Map<String,Object> getTaskStatusStatistics() {
        List<Object[]> rawStatus = taskRepository.getTaskStatusStatistics();

        Map<String,Object> formattedStats = new HashMap<>();
        formattedStats.put("totalTasks", taskRepository.count());

        List<Map<String,Object>> statusStats = rawStatus.stream()
                .map(row ->{
            Map<String,Object> stat = new HashMap<>();
            stat.put("status", row[0]);
            stat.put("count", row[1]);
            stat.put("oldestTask", row[2]);
            stat.put("newestTask", row[3]);
            return stat;

         })
                .collect(Collectors.toList());
        formattedStats.put("statusBreakdown", statusStats);
        return formattedStats;
     }

     public void addTaskCreationListener(Consumer<Task> taskCreationListener) {
        taskCreationListeners.add(taskCreationListener);
     }

     public void addTaskCompletionListener(Consumer<Task> taskCompletionListener) {
        taskCompletionListeners.add(taskCompletionListener);
     }

     public TaskStatistics getTaskStatistics() {
        long total = taskRepository.count();
        long pending = taskRepository.countByStatus(Task.TaskStatus.PENDING);
        long inProgress = taskRepository.countByStatus(Task.TaskStatus.IN_PROGRESS);
        long blocked = taskRepository.countByStatus(Task.TaskStatus.BLOCKED);
        long completed = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);

        return new TaskStatistics(total, pending, inProgress, blocked, completed);
     }

     private long countTasksByStatus(Task.TaskStatus status,List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .count();
     }

     public String generateTaskReport() {
        List<Task> tasks = taskRepository.findAll();
        int taskSize = tasks.size();
         return """
                TASK MANAGEMENT REPORT
                ----------------------
                Total Tasks: %d
                Pending: %d
                In Progress: %d
                Blocked: %d
                Completed: %d
                
                Last Updated: %s
                """.formatted(
                 taskSize,
                 countTasksByStatus(Task.TaskStatus.PENDING, tasks),
                 countTasksByStatus(Task.TaskStatus.IN_PROGRESS, tasks),
                 countTasksByStatus(Task.TaskStatus.BLOCKED, tasks),
                 countTasksByStatus(Task.TaskStatus.COMPLETED, tasks),
                 LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    public String getTaskDescription(Object taskIdentifier){
        switch (taskIdentifier){
            case UUID id ->{
                Task task = taskRepository.findById(id).get();
                return task != null ? task.getDescription() : "Task not found";
            }
            case String title -> {
                return findTaskByTitle(title)
                        .map(TaskResponseDTO::description)
                        .orElse("Task not found.");
            }
            default -> {
                return "Invalid task identifier.";
            }
        }
    }
    public CompletableFuture<List<TaskResponseDTO>> getTasksAsync() {
        return CompletableFuture.supplyAsync(this::getAllTasks, Executors.newVirtualThreadPerTaskExecutor());
    }

    public String describeTask(Object obj){
        return switch (obj){
            case UUID id ->{
                Task task = taskRepository.findById(id).get();
                yield task != null ? task.getTitle() : "Unknown Task";
            }
            case Task task -> "Task: " + task.getTitle() + "("+ task.getStatus() + ")";
            default -> "Unknown Object";
        };
    }

    public record TaskDuration(UUID id, Duration duration) {}

    public List<String> analyzeTaskDuration(List<TaskDuration> durations){
        return durations.stream()
                .map(duration -> switch (duration){
                    case TaskDuration(UUID id, Duration d) when d.toHours() < 1 ->
                        "Task"+ id + ": Quick task";
                    case TaskDuration(UUID id,Duration d) when  d.toHours() < 8 ->
                        "Task"+ id + ": Medium task";
                    case TaskDuration(UUID id, Duration d) ->
                        "Task"+ id + ": Longer task";
                })
                .toList();
    }

    public String getTaskSummary(UUID id){
        Task task = taskRepository.findById(id).get();
        if(task==null){
            throw  new IllegalArgumentException("Task not found with ID: " + id);
        }
        return "Task:" + task.getTitle()+"Status:"+task.getStatus();
    }

    public boolean hasTaskWithStatus(Task.TaskStatus status){
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .anyMatch(task -> switch (task) {
                    case Task t when t.getStatus() == status -> true;
                    default -> false;
                });

    }

    public Map<Task.TaskStatus,List<TaskResponseDTO>> groupTasksByStatus(){
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .collect(Collectors.groupingBy(
                        Task::getStatus,
                        Collectors.mapping(TaskResponseMapper.INSTANCE::toDTO, Collectors.toList())
                ));
    }

    public Task.Priority getTaskPriorityObject(UUID id) {
        Task task = taskRepository.findById(id).get();
        if(task == null){
            throw  new IllegalArgumentException("Task not found with ID: " + id);
        }

        return switch (task.getStatus()) {
            case PENDING, COMPLETED -> new Task.LowPriority();
            case IN_PROGRESS -> new Task.MediumPriority();
            case BLOCKED -> new Task.HighPriority();
        };
    }

    public Task updateTaskPriority(UUID id, Task.Priority priority){
        Task task = taskRepository.findById(id).get();
        if(task==null){
            throw  new IllegalArgumentException("Task not found with ID: " + id);
        }
        return task;
    }

}
