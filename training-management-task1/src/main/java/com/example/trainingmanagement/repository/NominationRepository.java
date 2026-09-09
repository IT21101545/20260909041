package com.example.trainingmanagement.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.trainingmanagement.entity.Nomination;

public interface NominationRepository extends JpaRepository<Nomination, Long> {
        boolean existsByOfficerIdAndTrainingIdAndNominatedAtAfterAndStatusNot(
            Long officerId, Long trainingId, LocalDateTime nominatedAt, String status);

            @Query("""
                    select case when count(n) > 0 then true else false end
                    from Nomination n
                    where n.officer.id = :officerId
                      and lower(n.training.title) = lower(:trainingTitle)
                      and n.status = :status
                      and n.training.date between :fromDate and :toDate
                    """)
            boolean existsRecentParticipation(
                    @Param("officerId") Long officerId,
                    @Param("trainingTitle") String trainingTitle,
                    @Param("status") String status,
                    @Param("fromDate") LocalDate fromDate,
                    @Param("toDate") LocalDate toDate);

    Optional<Nomination> findByOfficerIdAndTrainingId(Long officerId, Long trainingId);

    long countByTrainingIdAndStatus(Long trainingId, String status);

    Optional<Nomination> findFirstByTrainingIdAndStatusOrderByNominatedAtAsc(
            Long trainingId, String status);
}
