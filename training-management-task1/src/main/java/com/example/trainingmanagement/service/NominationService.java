package com.example.trainingmanagement.service;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.entity.Department;
import com.example.trainingmanagement.entity.Nomination;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.exception.DuplicateNominationException;
import com.example.trainingmanagement.repository.DepartmentRepository;
import com.example.trainingmanagement.repository.NominationRepository;
import com.example.trainingmanagement.repository.OfficerRepository;
import com.example.trainingmanagement.repository.TrainingRepository;

@Service
public class NominationService {

    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_WAITLISTED = "WAITLISTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private final NominationRepository nominationRepository;
    private final OfficerRepository officerRepository;
    private final TrainingRepository trainingRepository;
    private final DepartmentRepository departmentRepository;
    private final EligibilityService eligibilityService;

    public NominationService(
            NominationRepository nominationRepository,
            OfficerRepository officerRepository,
            TrainingRepository trainingRepository,
            DepartmentRepository departmentRepository,
            EligibilityService eligibilityService) {
        this.nominationRepository = nominationRepository;
        this.officerRepository = officerRepository;
        this.trainingRepository = trainingRepository;
        this.departmentRepository = departmentRepository;
        this.eligibilityService = eligibilityService;
    }

    @Transactional
    public Nomination create(NominationRequest request) {
        LocalDateTime duplicateCutoff = LocalDateTime.now().minus(Period.ofMonths(12));
        if (nominationRepository.existsByOfficerIdAndTrainingIdAndNominatedAtAfterAndStatusNot(
            request.getOfficerId(), request.getTrainingId(), duplicateCutoff, STATUS_CANCELLED)) {
            nominationRepository.findByOfficerIdAndTrainingId(request.getOfficerId(), request.getTrainingId())
                .ifPresent(existing -> {
                throw new DuplicateNominationException(String.format(
                    "This officer has already been nominated for this training programme "
                        + "by %s within the last 12 months.",
                    existing.getDepartment().getName()));
                });
        }

        Officer officer = officerRepository.findById(request.getOfficerId())
                .orElseThrow(() -> new IllegalArgumentException("Officer not found."));

        Training training = trainingRepository.findById(request.getTrainingId())
                .orElseThrow(() -> new IllegalArgumentException("Training programme not found."));

        eligibilityService.validate(officer, training);

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found."));

        Nomination nomination = new Nomination();
        nomination.setOfficer(officer);
        nomination.setTraining(training);
        nomination.setDepartment(department);
        nomination.setNominatedAt(LocalDateTime.now());

        // Task 2: seats are filled strictly in order of arrival. Anything beyond
        // the training's maxParticipants goes on the waiting list instead of
        // being rejected outright.
        long confirmedCount = nominationRepository.countByTrainingIdAndStatus(
                training.getId(), STATUS_CONFIRMED);

        nomination.setStatus(
                confirmedCount < training.getMaxParticipants()
                        ? STATUS_CONFIRMED
                        : STATUS_WAITLISTED
        );

        try {
            return nominationRepository.save(nomination);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateNominationException(
                "Duplicate nomination rejected. This officer is already nominated for this training."
            );
        }
    }

    /**
     * Task 2: cancelling a CONFIRMED nomination frees a seat. The longest-waiting
     * person on that training's waiting list (earliest nominatedAt) is promoted
     * to CONFIRMED automatically. Cancelling a WAITLISTED nomination simply
     * removes them from the queue.
     */
    @Transactional
    public Nomination cancel(Long nominationId) {
        Nomination nomination = nominationRepository.findById(nominationId)
                .orElseThrow(() -> new IllegalArgumentException("Nomination not found."));

        boolean wasConfirmed = STATUS_CONFIRMED.equals(nomination.getStatus());

        nomination.setStatus(STATUS_CANCELLED);
        nominationRepository.save(nomination);

        if (wasConfirmed) {
            nominationRepository.findFirstByTrainingIdAndStatusOrderByNominatedAtAsc(
                    nomination.getTraining().getId(), STATUS_WAITLISTED
            ).ifPresent(next -> {
                next.setStatus(STATUS_CONFIRMED);
                nominationRepository.save(next);
            });
        }

        return nomination;
    }

    public List<Nomination> findAll() {
        return nominationRepository.findAll();
    }
}
