package ch.bernet.trainingplatform.service.exercise;

import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.persistence.exercise.ExerciseRepository;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseUsageChecker exerciseUsageChecker;

    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    @DisplayName("UT-06 Übung wird für den angemeldeten Sportler gespeichert")
    void createExercise_valid_savesForUser() {
    }

    @Test
    @DisplayName("UT-06 Gleicher Name ohne Gross-/Kleinschreibung ergibt 409")
    void createExercise_duplicateNameIgnoringCase_throwsConflict() {
    }

    @Test
    @DisplayName("UT-06 Eigener Name beim Ändern ergibt keinen Konflikt")
    void updateExercise_ownName_noConflict() {
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Übung ergibt 404")
    void updateExercise_foreignOrDeleted_throwsNotFound() {
        // Arrange: die Übung gehört nicht Sportler A oder ist soft-gelöscht
        when(exerciseRepository.findActiveByIdAndUserId(BENCH_PRESS, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> exerciseService.updateExercise(USER_A, BENCH_PRESS, new ExerciseRequest("Bankdrücken", 1, null)))
                .isInstanceOf(NotFoundException.class);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-07 Unbenutzte Übung wird soft gelöscht")
    void deleteExercise_unused_softDeletes() {
    }

    @Test
    @DisplayName("UT-07 Übung in einer nicht gelöschten Routine ergibt 409")
    void deleteExercise_usedInActiveRoutine_throwsConflict() {
    }

}
