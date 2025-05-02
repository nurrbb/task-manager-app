package com.nurbb.taskmanagerapp.service;

import com.nurbb.taskmanagerapp.model.entity.Task;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ReactiveTaskService {

    private final Map<UUID, Task> taskStore = new HashMap<>();

// Multi-threading ile eşzamanlı tasklar için Thread sınıfı kullanılıyor. İşi tanımlamak için runnable interface
// verilir void run() metodunu barındıran bir interface yalnızca side-effect oluşturmak amacıyla kullanılıyor
// değer döndürmez ve checked excep fırlatamaz "Sonuç üreten ve hata yönetimi gerektiren işlemler için yetersiz.
// Java 5 ile birlikte Callable<T> interface tanıtıldı. İş tamamlandığında sonuç döndürebilir hata fırlatabilir
//  Reaktif'te ise klasik senkron işlemleri rekatif lazy ve non-blocking yapılara sarmak için kullanılır

    public Mono<Task> createTask(String title, String description){
        return Mono.fromCallable(() -> {
            if (title == null || title.trim().isEmpty()){
                throw new IllegalArgumentException("Task title cannot be empty");
            }
            Task task = Task.builder()
                    .title(title)
                    .description(description)
                    .build();
            taskStore.put(task.getId(),task);
            return task;
        });
    }

    // Flux: çok sayıda task nesnesini döndürür. Project Reactor lib ait bir Publisher interface'idir
    // fromItetable ise herhangi bir iterable veri kaynağını Flux'a döndürür. TaskStore hash map olarak tutuyorduk

    public Flux<Task> getAllTasks(){
        return Flux.fromIterable(taskStore.values())
                .delayElements(Duration.ofSeconds(1));
    }

    public Mono<Task> getTaskById(UUID id){
        return Mono.justOrEmpty(taskStore.get(id));
    } // görev bulunamazsa mono.empty() döner hata fırlatılmadan yönetilir.

    //switchIfEmpty bir önceki mono boşsa yine zinciri kırmaz ama hata fırlatır.
    public Mono<Task> updateTaskStatus(UUID id, Task.TaskStatus newStatus){
     return Mono.justOrEmpty(taskStore.get(id))
             .switchIfEmpty(Mono.error(new IllegalArgumentException("Task not found with id: " + id)))
             .map(task -> {
                 Task updatedTask = task.updateStatus(newStatus);
                 taskStore.put(id, updatedTask);
                 return updatedTask;
             });
    }

    public Mono<Void> deleteTask(UUID id){
        return Mono.fromRunnable(() -> taskStore.remove(id));
    }
}
