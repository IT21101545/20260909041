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

        when(nominationRepository.existsByOfficerIdAndTrainingId(1L, 10L))
                .thenReturn(true);

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
}
