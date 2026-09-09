package com.example.trainingmanagement.config;

import com.example.trainingmanagement.entity.*;
import com.example.trainingmanagement.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            DepartmentRepository departmentRepository,
            OfficerRepository officerRepository,
            TrainingRepository trainingRepository) {

        return args -> {
            if (departmentRepository.count() > 0) return;

            Department finance = departmentRepository.save(new Department("Finance Division"));
            Department admin = departmentRepository.save(new Department("Administration Division"));
            Department hr = departmentRepository.save(new Department("Human Resources Division"));

            officerRepository.save(new Officer("John Perera", "john@example.com", finance));
            officerRepository.save(new Officer("Sarah Fernando", "sarah@example.com", admin));
            officerRepository.save(new Officer("Kamal Silva", "kamal@example.com", hr));

            trainingRepository.save(new Training(
                    "Java Programming",
                    LocalDate.now().plusDays(10),
                    "Training Hall A",
                    50));

            trainingRepository.save(new Training(
                    "Spring Boot Development",
                    LocalDate.now().plusDays(20),
                    "Training Hall B",
                    40));

            trainingRepository.save(new Training(
                    "Software Testing",
                    LocalDate.now().plusDays(30),
                    "Computer Lab 1",
                    60));
        };
    }
}
