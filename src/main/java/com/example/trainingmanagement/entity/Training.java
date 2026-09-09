package com.example.trainingmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private Integer maxParticipants;

    public Training() {}

    public Training(String title, LocalDate date, String venue, Integer maxParticipants) {
        this.title = title;
        this.date = date;
        this.venue = venue;
        this.maxParticipants = maxParticipants;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getDate() { return date; }
    public String getVenue() { return venue; }
    public Integer getMaxParticipants() { return maxParticipants; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setVenue(String venue) { this.venue = venue; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
}
