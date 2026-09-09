package com.example.trainingmanagement;

import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.entity.*;
import com.example.trainingmanagement.exception.DuplicateNominationException;
import com.example.trainingmanagement.repository.*;
import com.example.trainingmanagement.service.NominationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NominationServiceTest {

    @Test
    void duplicateOfficerAndTrainingShouldBeRejected() {
        NominationRepository nominationRepository = mock(NominationRepository.class);
        OfficerRepository officerRepository = mock(OfficerRepository.class);
        TrainingRepository trainingRepository = mock(TrainingRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);

        Department financeDept = new Department("Finance Division");
        Officer officer = new Officer("A. Perera", "perera@example.com", financeDept);
        Training training = new Training("Java Programming", java.time.LocalDate.now(), "Hall A", 30);

        Nomination existingNomination = new Nomination();
        existingNomination.setOfficer(officer);
        existingNomination.setTraining(training);
        existingNomination.setDepartment(financeDept);

        when(nominationRepository.findByOfficerIdAndTrainingId(1L, 10L))
                .thenReturn(java.util.Optional.of(existingNomination));

        NominationService service = new NominationService(
                nominationRepository,
                officerRepository,
                trainingRepository,
                departmentRepository
        );

        NominationRequest request = new NominationRequest();
        request.setOfficerId(1L);
        request.setTrainingId(10L);
        request.setDepartmentId(2L);

        assertThrows(
                DuplicateNominationException.class,
                () -> service.create(request)
        );

        verify(nominationRepository, never()).save(any());
    }

    @Test
    void nominationBeyondCapacityShouldBeWaitlisted() {
        NominationRepository nominationRepository = mock(NominationRepository.class);
        OfficerRepository officerRepository = mock(OfficerRepository.class);
        TrainingRepository trainingRepository = mock(TrainingRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);

        Department dept = new Department("Finance Division");
        Officer officer = new Officer("A. Perera", "perera@example.com", dept);
        Training training = new Training("Cybersecurity Awareness", java.time.LocalDate.now(), "Hall A", 40);
        training.setId(10L);
        officer.setId(1L);

        when(nominationRepository.findByOfficerIdAndTrainingId(1L, 10L))
                .thenReturn(java.util.Optional.empty());
        when(officerRepository.findById(1L)).thenReturn(java.util.Optional.of(officer));
        when(trainingRepository.findById(10L)).thenReturn(java.util.Optional.of(training));
        when(departmentRepository.findById(2L)).thenReturn(java.util.Optional.of(dept));
        // 40 seats already confirmed -> capacity is full
        when(nominationRepository.countByTrainingIdAndStatus(10L, NominationService.STATUS_CONFIRMED))
                .thenReturn(40L);
        when(nominationRepository.save(any(Nomination.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NominationService service = new NominationService(
                nominationRepository, officerRepository, trainingRepository, departmentRepository);

        NominationRequest request = new NominationRequest();
        request.setOfficerId(1L);
        request.setTrainingId(10L);
        request.setDepartmentId(2L);

        Nomination result = service.create(request);

        org.junit.jupiter.api.Assertions.assertEquals(
                NominationService.STATUS_WAITLISTED, result.getStatus());
    }

    @Test
    void cancellingConfirmedNominationPromotesNextWaitlisted() {
        NominationRepository nominationRepository = mock(NominationRepository.class);
        OfficerRepository officerRepository = mock(OfficerRepository.class);
        TrainingRepository trainingRepository = mock(TrainingRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);

        Training training = new Training("Cybersecurity Awareness", java.time.LocalDate.now(), "Hall A", 40);
        training.setId(10L);

        Nomination confirmed = new Nomination();
        confirmed.setId(1L);
        confirmed.setTraining(training);
        confirmed.setStatus(NominationService.STATUS_CONFIRMED);

        Nomination nextInLine = new Nomination();
        nextInLine.setId(2L);
        nextInLine.setTraining(training);
        nextInLine.setStatus(NominationService.STATUS_WAITLISTED);

        when(nominationRepository.findById(1L)).thenReturn(java.util.Optional.of(confirmed));
        when(nominationRepository.findFirstByTrainingIdAndStatusOrderByNominatedAtAsc(
                10L, NominationService.STATUS_WAITLISTED))
                .thenReturn(java.util.Optional.of(nextInLine));
        when(nominationRepository.save(any(Nomination.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NominationService service = new NominationService(
                nominationRepository, officerRepository, trainingRepository, departmentRepository);

        service.cancel(1L);

        org.junit.jupiter.api.Assertions.assertEquals(
                NominationService.STATUS_CANCELLED, confirmed.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(
                NominationService.STATUS_CONFIRMED, nextInLine.getStatus());
    }
}
