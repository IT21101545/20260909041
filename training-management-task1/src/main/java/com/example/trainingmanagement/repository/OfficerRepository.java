package com.example.trainingmanagement.repository;

import com.example.trainingmanagement.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficerRepository extends JpaRepository<Officer, Long> {
}
