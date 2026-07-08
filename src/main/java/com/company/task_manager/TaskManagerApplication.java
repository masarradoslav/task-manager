package com.company.task_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagerApplication {

    // Boots the Spring application context and embedded web server.
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }

}
