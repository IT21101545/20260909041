package com.example.trainingmanagement.controller;

import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.repository.OfficerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {
    private final OfficerRepository repository;

    public OfficerController(OfficerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Officer> getAll() {
        return repository.findAll();
    }
}
