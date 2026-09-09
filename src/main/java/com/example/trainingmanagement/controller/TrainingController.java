package com.example.trainingmanagement.controller;

import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.repository.TrainingRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {
    private final TrainingRepository repository;

    public TrainingController(TrainingRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Training> getAll() {
        return repository.findAll();
    }
}
