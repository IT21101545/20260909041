package com.example.trainingmanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.repository.EligibilityRuleRepository;

@RestController
@RequestMapping("/api/eligibility-rules")
public class EligibilityRuleController {
    private final EligibilityRuleRepository repository;

    public EligibilityRuleController(EligibilityRuleRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/training/{trainingId}")
    public List<Map<String, String>> getForTraining(@PathVariable Long trainingId) {
        return repository.findByTrainingId(trainingId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Map<String, String> toResponse(EligibilityRule rule) {
        return Map.of(
                "ruleType", rule.getRuleType().name(),
                "ruleValue", rule.getRuleValue());
    }
}