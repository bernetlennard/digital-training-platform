package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.common.ErrorMessages;
import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.RoutineService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionAccessTest {

    @Mock
    private RoutineService routineService;

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private SessionAccess sessionAccess;

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Routine ergibt beim Planen und Starten 404")
    void getActiveRoutine_foreignOrDeleted_throwsNotFound() {
        // Arrange
        when(routineService.getRoutine(USER_A, ROUTINE_ID)).thenThrow(new NotFoundException(ErrorMessages.ROUTINE_NOT_FOUND));

        // Act & Assert
        assertThatThrownBy(() -> sessionAccess.getActiveRoutine(USER_A, ROUTINE_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("UT-15 Routinen werden inklusive gelöschter nach ID geliefert")
    void getRoutineSummaries_includesDeletedRoutinesById() {
        // Arrange
        RoutineSummary push = new RoutineSummary(ROUTINE_ID, "Push", false);
        RoutineSummary legs = new RoutineSummary(21L, "Beine", true);
        when(routineService.findAllSummaries(USER_A)).thenReturn(List.of(push, legs));

        // Act
        Map<Long, RoutineSummary> response = sessionAccess.getRoutineSummaries(USER_A);

        // Assert
        assertThat(response).containsOnly(Map.entry(ROUTINE_ID, push), Map.entry(21L, legs));
    }

    @Test
    @DisplayName("UT-15 Übungsnamen werden auch für gelöschte Übungen aufgelöst")
    void getExercises_delegatesToExerciseService() {
        // Arrange
        Map<Long, ExerciseResponse> exercises = Map.of(
                BENCH_PRESS, new ExerciseResponse(BENCH_PRESS, "Bankdrücken", 1, "Brust", null));
        when(exerciseService.findByIds(USER_A, List.of(BENCH_PRESS))).thenReturn(exercises);

        // Act & Assert
        assertThat(sessionAccess.getExercises(USER_A, List.of(BENCH_PRESS))).isEqualTo(exercises);
    }

}
