package com.example.trainingmanagement.controller;

import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.entity.Nomination;
import com.example.trainingmanagement.service.NominationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nominations")
public class NominationController {
    private final NominationService nominationService;

    public NominationController(NominationService nominationService) {
        this.nominationService = nominationService;
    }

    @GetMapping
    public List<Nomination> getAll() {
        return nominationService.findAll();
    }

    @PostMapping
    public ResponseEntity<Nomination> create(@Valid @RequestBody NominationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nominationService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Nomination> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(nominationService.cancel(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(com.example.trainingmanagement.exception.DuplicateNominationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(
            com.example.trainingmanagement.exception.DuplicateNominationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }
}
