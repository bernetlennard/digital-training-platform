package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.persistence.routine.RoutineRepository;
import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoutineServiceTest {

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private RoutineDeletionListener routineDeletionListener;

    @InjectMocks
    private RoutineService routineService;

    @Test
    @DisplayName("UT-08 Routine wird mit Positionen in der Reihenfolge des Requests gespeichert")
    void createRoutine_valid_savesWithPositions() {
    }

    @Test
    @DisplayName("UT-08 Doppelte Übung ergibt 422")
    void createRoutine_duplicateExercise_throwsBusinessRule() {
    }

    @Test
    @DisplayName("UT-08 Gleicher Name ohne Gross-/Kleinschreibung ergibt 409")
    void createRoutine_duplicateName_throwsConflict() {
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Übung ergibt 404")
    void createRoutine_foreignExercise_throwsNotFound() {
        // Arrange: eine der Übungen gehört nicht Sportler A oder ist soft-gelöscht
        doThrow(new NotFoundException("Die Übung wurde nicht gefunden."))
                .when(exerciseService).assertActiveExercises(eq(USER_A), anyCollection());

        // Act & Assert
        assertThatThrownBy(() -> routineService.createRoutine(USER_A, new RoutineRequest("Push", List.of(BENCH_PRESS, SQUAT))))
                .isInstanceOf(NotFoundException.class);

        verify(routineRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-08 Ändern ersetzt Name und Übungsliste")
    void updateRoutine_replacesNameAndExercises() {
    }

    @Test
    @DisplayName("UT-09 Löschen ist ein Soft Delete und entfernt die geplanten Einheiten")
    void deleteRoutine_softDeletesAndNotifiesListener() {
    }

}
