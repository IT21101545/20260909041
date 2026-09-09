package com.example.trainingmanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "nominations")
public class Nomination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "officer_id")
    private Officer officer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_id")
    private Training training;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    // CONFIRMED | WAITLISTED | CANCELLED
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime nominatedAt;

    public Nomination() {}

    public Long getId() { return id; }
    public Officer getOfficer() { return officer; }
    public Training getTraining() { return training; }
    public Department getDepartment() { return department; }
    public String getStatus() { return status; }
    public LocalDateTime getNominatedAt() { return nominatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setOfficer(Officer officer) { this.officer = officer; }
    public void setTraining(Training training) { this.training = training; }
    public void setDepartment(Department department) { this.department = department; }
    public void setStatus(String status) { this.status = status; }
    public void setNominatedAt(LocalDateTime nominatedAt) { this.nominatedAt = nominatedAt; }
}
