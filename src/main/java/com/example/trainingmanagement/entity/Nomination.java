package com.example.trainingmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "nominations",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_officer_training",
        columnNames = {"officer_id", "training_id"}
    )
)
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

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDate nominatedDate;

    public Nomination() {}

    public Long getId() { return id; }
    public Officer getOfficer() { return officer; }
    public Training getTraining() { return training; }
    public Department getDepartment() { return department; }
    public String getStatus() { return status; }
    public LocalDate getNominatedDate() { return nominatedDate; }

    public void setId(Long id) { this.id = id; }
    public void setOfficer(Officer officer) { this.officer = officer; }
    public void setTraining(Training training) { this.training = training; }
    public void setDepartment(Department department) { this.department = department; }
    public void setStatus(String status) { this.status = status; }
    public void setNominatedDate(LocalDate nominatedDate) { this.nominatedDate = nominatedDate; }
}
