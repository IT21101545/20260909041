package com.example.trainingmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trainingmanagement.entity.EligibilityRule;

public interface EligibilityRuleRepository extends JpaRepository<EligibilityRule, Long> {
    List<EligibilityRule> findByTrainingId(Long trainingId);

    boolean existsByTrainingIdAndRuleTypeAndRuleValue(
            Long trainingId, com.example.trainingmanagement.entity.EligibilityRuleType ruleType,
            String ruleValue);
}