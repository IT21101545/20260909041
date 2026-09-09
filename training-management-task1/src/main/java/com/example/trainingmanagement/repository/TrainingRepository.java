package com.example.trainingmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trainingmanagement.entity.Training;

public interface TrainingRepository extends JpaRepository<Training, Long> {
	Optional<Training> findByTitle(String title);
}
