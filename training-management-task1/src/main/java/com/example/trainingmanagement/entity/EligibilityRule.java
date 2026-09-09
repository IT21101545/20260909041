package com.example.trainingmanagement.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "eligibility_rules")
public class EligibilityRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_id")
    private Training training;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EligibilityRuleType ruleType;

    @Column(nullable = false)
    private String ruleValue;

    public EligibilityRule() {}

    public EligibilityRule(Training training, EligibilityRuleType ruleType, String ruleValue) {
        this.training = training;
        this.ruleType = ruleType;
        this.ruleValue = ruleValue;
    }

    public Long getId() { return id; }
    public Training getTraining() { return training; }
    public EligibilityRuleType getRuleType() { return ruleType; }
    public String getRuleValue() { return ruleValue; }

    public void setId(Long id) { this.id = id; }
    public void setTraining(Training training) { this.training = training; }
    public void setRuleType(EligibilityRuleType ruleType) { this.ruleType = ruleType; }
    public void setRuleValue(String ruleValue) { this.ruleValue = ruleValue; }
}