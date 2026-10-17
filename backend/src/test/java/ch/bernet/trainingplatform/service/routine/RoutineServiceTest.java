package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.common.BusinessRuleException;
import ch.bernet.trainingplatform.common.ConflictException;
import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.persistence.routine.Routine;
import ch.bernet.trainingplatform.persistence.routine.RoutineExercise;
import ch.bernet.trainingplatform.persistence.routine.RoutineRepository;
import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutineServiceTest {

    private static final Map<Long, ExerciseResponse> EXERCISES = Map.of(
            BENCH_PRESS, new ExerciseResponse(BENCH_PRESS, "Bankdrücken", 1, "Brust", null),
            SQUAT, new ExerciseResponse(SQUAT, "Kniebeuge", 7, "Beine", null),
            PULL_UP, new ExerciseResponse(PULL_UP, "Klimmzug", 2, "Rücken", null));

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private ExerciseService exerciseService;

    @Mock
    private RoutineDeletionListener routineDeletionListener;

    @InjectMocks
    private RoutineService routineService;

    @Test
    @DisplayName("UT-08 Routinen werden mit ihren Übungen in Reihenfolge geliefert")
    void getRoutines_returnsRoutinesWithExercises() {
        // Arrange
        when(routineRepository.findActiveByUserId(USER_A)).thenReturn(List.of(routine(ROUTINE_ID, "Push", BENCH_PRESS, SQUAT)));
        when(exerciseService.findByIds(eq(USER_A), anyCollection())).thenReturn(EXERCISES);

        // Act
        List<RoutineResponse> response = routineService.getRoutines(USER_A);

        // Assert
        assertThat(response).containsExactly(new RoutineResponse(ROUTINE_ID, "Push", List.of(
                new RoutineExerciseResponse(1, BENCH_PRESS, "Bankdrücken", "Brust"),
                new RoutineExerciseResponse(2, SQUAT, "Kniebeuge", "Beine"))));
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Routine lesen ergibt 404")
    void getRoutine_foreignOrDeleted_throwsNotFound() {
        // Arrange
        when(routineRepository.findActiveByIdAndUserId(ROUTINE_ID, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> routineService.getRoutine(USER_A, ROUTINE_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("UT-08 Routine wird mit Positionen in der Reihenfolge des Requests gespeichert")
    void createRoutine_valid_savesWithPositions() {
        // Arrange
        when(routineRepository.save(any(Routine.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), ROUTINE_ID));
        when(exerciseService.findByIds(eq(USER_A), anyCollection())).thenReturn(EXERCISES);

        // Act
        RoutineResponse response = routineService.createRoutine(USER_A, new RoutineRequest("Push", List.of(SQUAT, BENCH_PRESS)));

        // Assert
        ArgumentCaptor<Routine> captor = ArgumentCaptor.forClass(Routine.class);
        verify(routineRepository).save(captor.capture());
        Routine saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(USER_A);
        assertThat(saved.getName()).isEqualTo("Push");
        assertThat(saved.isDeleted()).isFalse();
        assertThat(saved.getExercises())
                .extracting(RoutineExercise::getExerciseId, RoutineExercise::getPosition)
                .containsExactly(tuple(SQUAT, 1), tuple(BENCH_PRESS, 2));

        assertThat(response).isEqualTo(new RoutineResponse(ROUTINE_ID, "Push", List.of(
                new RoutineExerciseResponse(1, SQUAT, "Kniebeuge", "Beine"),
                new RoutineExerciseResponse(2, BENCH_PRESS, "Bankdrücken", "Brust"))));

        // Die Übungen müssen eigene, aktive Übungen sein
        verify(exerciseService).assertActiveExercises(eq(USER_A),
                argThat((Collection<Long> ids) -> ids.size() == 2 && ids.containsAll(List.of(SQUAT, BENCH_PRESS))));
    }

    @Test
    @DisplayName("UT-08 Doppelte Übung ergibt 422")
    void createRoutine_duplicateExercise_throwsBusinessRule() {
        // Act & Assert
        assertThatThrownBy(() -> routineService.createRoutine(USER_A,
                new RoutineRequest("Push", List.of(BENCH_PRESS, SQUAT, BENCH_PRESS))))
                .isInstanceOf(BusinessRuleException.class);

        verify(routineRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-08 Gleicher Name ohne Gross-/Kleinschreibung ergibt 409")
    void createRoutine_duplicateName_throwsConflict() {
        // Arrange: das Repository vergleicht ohne Gross-/Kleinschreibung und nur aktive Routinen (#FA-3.4)
        when(routineRepository.findActiveByUserIdAndName(eq(USER_A), argThat("Push"::equalsIgnoreCase)))
                .thenReturn(Optional.of(routine(ROUTINE_ID, "Push", BENCH_PRESS)));

        // Act & Assert
        assertThatThrownBy(() -> routineService.createRoutine(USER_A, new RoutineRequest("PUSH", List.of(SQUAT))))
                .isInstanceOf(ConflictException.class);

        verify(routineRepository, never()).save(any());
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
        // Arrange
        Routine routine = routine(ROUTINE_ID, "Push", BENCH_PRESS, SQUAT);
        when(routineRepository.findActiveByIdAndUserId(ROUTINE_ID, USER_A)).thenReturn(Optional.of(routine));
        when(exerciseService.findByIds(eq(USER_A), anyCollection())).thenReturn(EXERCISES);
        // Ob save aufgerufen wird, bleibt der Umsetzung überlassen
        lenient().when(routineRepository.save(any(Routine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RoutineResponse response = routineService.updateRoutine(USER_A, ROUTINE_ID,
                new RoutineRequest("Push schwer", List.of(PULL_UP, BENCH_PRESS)));

        // Assert (#FA-3.3, #FA-3.6)
        assertThat(routine.getName()).isEqualTo("Push schwer");
        assertThat(routine.getExercises())
                .extracting(RoutineExercise::getExerciseId, RoutineExercise::getPosition)
                .containsExactly(tuple(PULL_UP, 1), tuple(BENCH_PRESS, 2));
        assertThat(response).isEqualTo(new RoutineResponse(ROUTINE_ID, "Push schwer", List.of(
                new RoutineExerciseResponse(1, PULL_UP, "Klimmzug", "Rücken"),
                new RoutineExerciseResponse(2, BENCH_PRESS, "Bankdrücken", "Brust"))));
        verify(exerciseService).assertActiveExercises(eq(USER_A),
                argThat((Collection<Long> ids) -> ids.size() == 2 && ids.containsAll(List.of(PULL_UP, BENCH_PRESS))));
    }

    @Test
    @DisplayName("UT-08 Eigener Name beim Ändern ergibt keinen Konflikt")
    void updateRoutine_ownName_noConflict() {
        // Arrange: nur die Schreibweise ändert sich, das Repository findet die Routine selbst
        Routine routine = routine(ROUTINE_ID, "Push", BENCH_PRESS);
        when(routineRepository.findActiveByIdAndUserId(ROUTINE_ID, USER_A)).thenReturn(Optional.of(routine));
        when(routineRepository.findActiveByUserIdAndName(eq(USER_A), argThat("Push"::equalsIgnoreCase)))
                .thenReturn(Optional.of(routine));
        when(exerciseService.findByIds(eq(USER_A), anyCollection())).thenReturn(EXERCISES);
        lenient().when(routineRepository.save(any(Routine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RoutineResponse response = routineService.updateRoutine(USER_A, ROUTINE_ID, new RoutineRequest("PUSH", List.of(BENCH_PRESS)));

        // Assert
        assertThat(routine.getName()).isEqualTo("PUSH");
        assertThat(response.name()).isEqualTo("PUSH");
    }

    @Test
    @DisplayName("UT-08 Name einer anderen Routine beim Ändern ergibt 409")
    void updateRoutine_nameOfOtherRoutine_throwsConflict() {
        // Arrange
        Routine routine = routine(ROUTINE_ID, "Push", BENCH_PRESS);
        when(routineRepository.findActiveByIdAndUserId(ROUTINE_ID, USER_A)).thenReturn(Optional.of(routine));
        when(routineRepository.findActiveByUserIdAndName(eq(USER_A), argThat("Beine"::equalsIgnoreCase)))
                .thenReturn(Optional.of(routine(21L, "Beine", SQUAT)));

        // Act & Assert
        assertThatThrownBy(() -> routineService.updateRoutine(USER_A, ROUTINE_ID, new RoutineRequest("beine", List.of(SQUAT))))
                .isInstanceOf(ConflictException.class);

        assertThat(routine.getName()).isEqualTo("Push");
        assertThat(routine.getExercises()).extracting(RoutineExercise::getExerciseId).containsExactly(BENCH_PRESS);
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Routine ändern ergibt 404")
    void updateRoutine_foreignOrDeleted_throwsNotFound() {
        // Arrange
        when(routineRepository.findActiveByIdAndUserId(ROUTINE_ID, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> routineService.updateRoutine(USER_A, ROUTINE_ID, new RoutineRequest("Push", List.of(BENCH_PRESS))))
                .isInstanceOf(NotFoundException.class);

        verify(routineRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-09 Löschen ist ein Soft Delete und entfernt die geplanten Einheiten")
    void deleteRoutine_softDeletesAndNotifiesListener() {
        // Arrange
        Routine routine = routine(ROUTINE_ID, "Push", BENCH_PRESS);
        when(routineRepository.findActiveByIdAndUserId(ROUTINE_ID, USER_A)).thenReturn(Optional.of(routine));

        // Act
        routineService.deleteRoutine(USER_A, ROUTINE_ID);

        // Assert: die Routine bleibt für die Historie erhalten (#FA-3.7)
        assertThat(routine.isDeleted()).isTrue();
        assertThat(routine.getExercises()).isNotEmpty();
        verify(routineDeletionListener).onRoutineDeleted(USER_A, ROUTINE_ID);
        verify(routineRepository, never()).delete(any());
        verify(routineRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Routine löschen ergibt 404")
    void deleteRoutine_foreignOrDeleted_throwsNotFound() {
        // Arrange
        when(routineRepository.findActiveByIdAndUserId(ROUTINE_ID, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> routineService.deleteRoutine(USER_A, ROUTINE_ID))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(routineDeletionListener);
    }

    @Test
    @DisplayName("UT-15 Zusammenfassungen enthalten auch gelöschte Routinen")
    void findAllSummaries_includesDeletedRoutines() {
        // Arrange
        Routine deleted = routine(21L, "Beine", SQUAT);
        deleted.setDeleted(true);
        when(routineRepository.findAllByUserId(USER_A)).thenReturn(List.of(routine(ROUTINE_ID, "Push", BENCH_PRESS), deleted));

        // Act
        List<RoutineSummary> response = routineService.findAllSummaries(USER_A);

        // Assert (#FA-6.3)
        assertThat(response).containsExactlyInAnyOrder(
                new RoutineSummary(ROUTINE_ID, "Push", false),
                new RoutineSummary(21L, "Beine", true));
    }

    // Übungen direkt anhängen, damit die Tests nicht von Routine.replaceExercises abhängen
    private static Routine routine(Long id, String name, Long... exerciseIds) {
        Routine routine = withId(new Routine(USER_A, name), id);
        for (int i = 0; i < exerciseIds.length; i++) {
            routine.getExercises().add(new RoutineExercise(exerciseIds[i], i + 1));
        }
        return routine;
    }

}
