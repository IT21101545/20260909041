package com.example.trainingmanagement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.trainingmanagement.dto.NominationRequest;
import com.example.trainingmanagement.entity.Department;
import com.example.trainingmanagement.entity.EligibilityRule;
import com.example.trainingmanagement.entity.EligibilityRuleType;
import com.example.trainingmanagement.entity.Nomination;
import com.example.trainingmanagement.entity.Officer;
import com.example.trainingmanagement.entity.Training;
import com.example.trainingmanagement.exception.DuplicateNominationException;
import com.example.trainingmanagement.repository.DepartmentRepository;
import com.example.trainingmanagement.repository.EligibilityRuleRepository;
import com.example.trainingmanagement.repository.NominationRepository;
import com.example.trainingmanagement.repository.OfficerRepository;
import com.example.trainingmanagement.repository.TrainingRepository;
import com.example.trainingmanagement.service.EligibilityService;
import com.example.trainingmanagement.service.NominationService;

class NominationServiceTest {

    @Test
    void duplicateOfficerAndTrainingShouldBeRejected() {
        NominationRepository nominationRepository = mock(NominationRepository.class);
        OfficerRepository officerRepository = mock(OfficerRepository.class);
        TrainingRepository trainingRepository = mock(TrainingRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        EligibilityService eligibilityService = new EligibilityService(mock(EligibilityRuleRepository.class));

        Department financeDept = new Department("Finance Division");
        Officer officer = new Officer("A. Perera", "perera@example.com", financeDept);
        Training training = new Training("Java Programming", java.time.LocalDate.now(), "Hall A", 30);

        Nomination existingNomination = new Nomination();
        existingNomination.setOfficer(officer);
        existingNomination.setTraining(training);
        existingNomination.setDepartment(financeDept);

        when(nominationRepository.findByOfficerIdAndTrainingId(1L, 10L))
                .thenReturn(java.util.Optional.of(existingNomination));
        when(nominationRepository.existsByOfficerIdAndTrainingIdAndNominatedAtAfterAndStatusNot(
                eq(1L), eq(10L), any(java.time.LocalDateTime.class),
                eq(NominationService.STATUS_CANCELLED))).thenReturn(true);

        NominationService service = new NominationService(
                nominationRepository,
                officerRepository,
                trainingRepository,
                departmentRepository,
                eligibilityService
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
        EligibilityService eligibilityService = new EligibilityService(mock(EligibilityRuleRepository.class));

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
                nominationRepository, officerRepository, trainingRepository, departmentRepository,
                eligibilityService);

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
        EligibilityService eligibilityService = new EligibilityService(mock(EligibilityRuleRepository.class));

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
                nominationRepository, officerRepository, trainingRepository, departmentRepository,
                eligibilityService);

        service.cancel(1L);

        org.junit.jupiter.api.Assertions.assertEquals(
                NominationService.STATUS_CANCELLED, confirmed.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals(
                NominationService.STATUS_CONFIRMED, nextInLine.getStatus());
    }

        @Test
        void officerFromDisallowedDepartmentShouldBeRejected() {
                EligibilityRuleRepository ruleRepository = mock(EligibilityRuleRepository.class);
                Department finance = new Department("Finance Division");
                Department administration = new Department("Administration Division");
                Officer officer = new Officer("A. Perera", "perera@example.com", administration);
                Training training = new Training("Financial Management", java.time.LocalDate.now(), "Hall A", 30);
                training.setId(10L);

                when(ruleRepository.findByTrainingId(10L)).thenReturn(java.util.List.of(
                                new EligibilityRule(training, EligibilityRuleType.ALLOWED_DEPARTMENT, finance.getName())));

                EligibilityService service = new EligibilityService(ruleRepository);

                assertThrows(IllegalArgumentException.class, () -> service.validate(officer, training));
        }

        @Test
        void officerBelowMinimumServiceShouldBeRejected() {
                EligibilityRuleRepository ruleRepository = mock(EligibilityRuleRepository.class);
                Department department = new Department("Administration Division");
                Officer officer = new Officer("A. Perera", "perera@example.com", department,
                                "Manager", 2);
                Training training = new Training("Management Development", java.time.LocalDate.now(), "Hall A", 30);
                training.setId(10L);

                when(ruleRepository.findByTrainingId(10L)).thenReturn(java.util.List.of(
                                new EligibilityRule(training, EligibilityRuleType.MIN_YEARS_OF_SERVICE, "5")));

                EligibilityService service = new EligibilityService(ruleRepository);

                assertThrows(IllegalArgumentException.class, () -> service.validate(officer, training));
        }
}
