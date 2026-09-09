package com.example.trainingmanagement.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.entity.EligibilityRuleType;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.repository.EligibilityRuleRepository;
import com.example.trainingmanagement.repository.NominationRepository;

@Service
public class EligibilityService {
    private final EligibilityRuleRepository eligibilityRuleRepository;
    private final NominationRepository nominationRepository;

    public EligibilityService(EligibilityRuleRepository eligibilityRuleRepository) {
        this(eligibilityRuleRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public EligibilityService(EligibilityRuleRepository eligibilityRuleRepository,
                              NominationRepository nominationRepository) {
        this.eligibilityRuleRepository = eligibilityRuleRepository;
        this.nominationRepository = nominationRepository;
    }

    public void validate(Officer officer, Training training) {
        List<EligibilityRule> rules = eligibilityRuleRepository.findByTrainingId(training.getId());

        validateAllowedDepartment(officer, rules);
        validateAllowedDesignation(officer, rules);
        validateMinimumService(officer, rules);
        validateRecentParticipation(officer, training, rules);
    }

    private void validateAllowedDepartment(Officer officer, List<EligibilityRule> rules) {
        List<EligibilityRule> departmentRules = rules.stream()
                .filter(rule -> rule.getRuleType() == EligibilityRuleType.ALLOWED_DEPARTMENT)
                .toList();

        if (!departmentRules.isEmpty() && departmentRules.stream().noneMatch(rule ->
                rule.getRuleValue().equalsIgnoreCase(officer.getDepartment().getName()))) {
            reject();
        }
    }

    private void validateAllowedDesignation(Officer officer, List<EligibilityRule> rules) {
        List<EligibilityRule> designationRules = rules.stream()
                .filter(rule -> rule.getRuleType() == EligibilityRuleType.ALLOWED_DESIGNATION)
                .toList();

        if (!designationRules.isEmpty() && (officer.getDesignation() == null
                || designationRules.stream().noneMatch(rule ->
                rule.getRuleValue().equalsIgnoreCase(officer.getDesignation())))) {
            reject();
        }
    }

    private void validateMinimumService(Officer officer, List<EligibilityRule> rules) {
        rules.stream()
                .filter(rule -> rule.getRuleType() == EligibilityRuleType.MIN_YEARS_OF_SERVICE)
                .findFirst()
                .ifPresent(rule -> {
                    int minimumYears = Integer.parseInt(rule.getRuleValue());
                    if (officer.getYearsOfService() == null || officer.getYearsOfService() < minimumYears) {
                        reject();
                    }
                });
    }

    private void validateRecentParticipation(Officer officer, Training training,
                                              List<EligibilityRule> rules) {
        if (nominationRepository == null) {
            return;
        }

        rules.stream()
                .filter(rule -> rule.getRuleType() == EligibilityRuleType.NO_RECENT_PARTICIPATION_MONTHS)
                .findFirst()
                .ifPresent(rule -> {
                    int months = Integer.parseInt(rule.getRuleValue());
                        LocalDate today = LocalDate.now();
                        LocalDate cutoff = today.minus(Period.ofMonths(months));
                        if (nominationRepository.existsRecentParticipation(
                            officer.getId(), training.getTitle(), NominationService.STATUS_CONFIRMED,
                            cutoff, today)) {
                        throw new IllegalArgumentException(
                                "Officer participated in this training programme within the last "
                                        + months + " months.");
                    }
                });
    }

    private void reject() {
        throw new IllegalArgumentException("Officer is not eligible for this training programme.");
    }
}