package com.example.trainingmanagement.repository;

import com.example.trainingmanagement.entity.Nomination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NominationRepository extends JpaRepository<Nomination, Long> {
    boolean existsByOfficerIdAndTrainingId(Long officerId, Long trainingId);
}
