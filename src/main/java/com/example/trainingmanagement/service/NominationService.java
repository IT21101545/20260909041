package com.example.trainingmanagement.service;

import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.entity.*;
import com.example.trainingmanagement.exception.DuplicateNominationException;
import com.example.trainingmanagement.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class NominationService {
    private final NominationRepository nominationRepository;
    private final OfficerRepository officerRepository;
    private final TrainingRepository trainingRepository;
    private final DepartmentRepository departmentRepository;

    public NominationService(
            NominationRepository nominationRepository,
            OfficerRepository officerRepository,
            TrainingRepository trainingRepository,
            DepartmentRepository departmentRepository) {
        this.nominationRepository = nominationRepository;
        this.officerRepository = officerRepository;
        this.trainingRepository = trainingRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public Nomination create(NominationRequest request) {
        if (nominationRepository.existsByOfficerIdAndTrainingId(
                request.getOfficerId(), request.getTrainingId())) {
            throw new DuplicateNominationException(
                "This officer has already been nominated for this training programme."
            );
        }

        Officer officer = officerRepository.findById(request.getOfficerId())
                .orElseThrow(() -> new IllegalArgumentException("Officer not found."));

        Training training = trainingRepository.findById(request.getTrainingId())
                .orElseThrow(() -> new IllegalArgumentException("Training programme not found."));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found."));

        Nomination nomination = new Nomination();
        nomination.setOfficer(officer);
        nomination.setTraining(training);
        nomination.setDepartment(department);
        nomination.setStatus("NOMINATED");
        nomination.setNominatedDate(LocalDate.now());

        try {
            return nominationRepository.save(nomination);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateNominationException(
                "Duplicate nomination rejected. This officer is already nominated for this training."
            );
        }
    }

    public List<Nomination> findAll() {
        return nominationRepository.findAll();
    }
}
