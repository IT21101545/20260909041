package com.example.trainingmanagement.repository;

import com.example.trainingmanagement.entity.Nomination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NominationRepository extends JpaRepository<Nomination, Long> {
    boolean existsByOfficerIdAndTrainingId(Long officerId, Long trainingId);

    Optional<Nomination> findByOfficerIdAndTrainingId(Long officerId, Long trainingId);

    long countByTrainingIdAndStatus(Long trainingId, String status);

    Optional<Nomination> findFirstByTrainingIdAndStatusOrderByNominatedAtAsc(
            Long trainingId, String status);
}
