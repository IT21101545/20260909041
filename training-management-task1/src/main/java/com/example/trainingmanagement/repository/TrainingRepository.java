package com.example.trainingmanagement.repository;

import com.example.trainingmanagement.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRepository extends JpaRepository<Training, Long> {
}
