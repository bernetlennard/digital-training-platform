package ch.bernet.trainingplatform.service.exercise;

import ch.bernet.trainingplatform.common.ConflictException;
import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.persistence.exercise.Exercise;
import ch.bernet.trainingplatform.persistence.exercise.ExerciseRepository;
import ch.bernet.trainingplatform.persistence.exercise.MuscleGroup;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseRequest;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.exercise.dto.MuscleGroupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
    @DisplayName("UT-06 Katalog liefert die aktiven Übungen mit Muskelgruppe")
    void getExercises_returnsActiveExercisesWithMuscleGroup() {
        // Arrange
        when(exerciseRepository.findActiveByUserId(USER_A)).thenReturn(List.of(
                exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST),
                exercise(SQUAT, "Kniebeuge", MuscleGroup.LEGS)));

        // Act
        List<ExerciseResponse> response = exerciseService.getExercises(USER_A);

        // Assert
        assertThat(response).containsExactly(
                new ExerciseResponse(BENCH_PRESS, "Bankdrücken", 1, "Brust", null),
                new ExerciseResponse(SQUAT, "Kniebeuge", 7, "Beine", null));
    }

    @Test
    @DisplayName("UT-06 Muskelgruppen sind eine feste Liste mit festen IDs")
    void getMuscleGroups_returnsFixedList() {
        // Act
        List<MuscleGroupResponse> response = exerciseService.getMuscleGroups();

        // Assert (#FA-2.2)
        assertThat(response).containsExactly(
                new MuscleGroupResponse(1, "Brust"),
                new MuscleGroupResponse(2, "Rücken"),
                new MuscleGroupResponse(3, "Schultern"),
                new MuscleGroupResponse(4, "Bizeps"),
                new MuscleGroupResponse(5, "Trizeps"),
                new MuscleGroupResponse(6, "Bauch"),
                new MuscleGroupResponse(7, "Beine"),
                new MuscleGroupResponse(8, "Gesäss"));
    }

    @Test
    @DisplayName("UT-06 Übung wird für den angemeldeten Sportler gespeichert")
    void createExercise_valid_savesForUser() {
        // Arrange
        when(exerciseRepository.save(any(Exercise.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), BENCH_PRESS));

        // Act
        ExerciseResponse response = exerciseService.createExercise(USER_A,
                new ExerciseRequest("Bankdrücken", 1, "Flachbank mit Langhantel"));

        // Assert
        ArgumentCaptor<Exercise> captor = ArgumentCaptor.forClass(Exercise.class);
        verify(exerciseRepository).save(captor.capture());
        Exercise saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(USER_A);
        assertThat(saved.getName()).isEqualTo("Bankdrücken");
        assertThat(saved.getMuscleGroup()).isEqualTo(MuscleGroup.CHEST);
        assertThat(saved.getDescription()).isEqualTo("Flachbank mit Langhantel");
        assertThat(saved.isDeleted()).isFalse();
        assertThat(response).isEqualTo(
                new ExerciseResponse(BENCH_PRESS, "Bankdrücken", 1, "Brust", "Flachbank mit Langhantel"));
    }

    @Test
    @DisplayName("UT-06 Gleicher Name ohne Gross-/Kleinschreibung ergibt 409")
    void createExercise_duplicateNameIgnoringCase_throwsConflict() {
        // Arrange: das Repository vergleicht ohne Gross-/Kleinschreibung und nur aktive Übungen (#FA-2.5)
        when(exerciseRepository.findActiveByUserIdAndName(eq(USER_A), argThat("Bankdrücken"::equalsIgnoreCase)))
                .thenReturn(Optional.of(exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST)));

        // Act & Assert
        assertThatThrownBy(() -> exerciseService.createExercise(USER_A, new ExerciseRequest("BANKDRÜCKEN", 1, null)))
                .isInstanceOf(ConflictException.class);

        verify(exerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-06 Eigener Name beim Ändern ergibt keinen Konflikt")
    void updateExercise_ownName_noConflict() {
        // Arrange: nur die Schreibweise ändert sich, das Repository findet die Übung selbst
        Exercise benchPress = exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST);
        when(exerciseRepository.findActiveByIdAndUserId(BENCH_PRESS, USER_A)).thenReturn(Optional.of(benchPress));
        when(exerciseRepository.findActiveByUserIdAndName(eq(USER_A), argThat("Bankdrücken"::equalsIgnoreCase)))
                .thenReturn(Optional.of(benchPress));
        // Ob save aufgerufen wird, bleibt der Umsetzung überlassen
        lenient().when(exerciseRepository.save(any(Exercise.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ExerciseResponse response = exerciseService.updateExercise(USER_A, BENCH_PRESS,
                new ExerciseRequest("BANKDRÜCKEN", 3, "Schrägbank"));

        // Assert (#FA-2.4)
        assertThat(benchPress.getName()).isEqualTo("BANKDRÜCKEN");
        assertThat(benchPress.getMuscleGroup()).isEqualTo(MuscleGroup.SHOULDERS);
        assertThat(benchPress.getDescription()).isEqualTo("Schrägbank");
        assertThat(response).isEqualTo(new ExerciseResponse(BENCH_PRESS, "BANKDRÜCKEN", 3, "Schultern", "Schrägbank"));
    }

    @Test
    @DisplayName("UT-06 Name einer anderen Übung beim Ändern ergibt 409")
    void updateExercise_nameOfOtherExercise_throwsConflict() {
        // Arrange
        Exercise benchPress = exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST);
        when(exerciseRepository.findActiveByIdAndUserId(BENCH_PRESS, USER_A)).thenReturn(Optional.of(benchPress));
        when(exerciseRepository.findActiveByUserIdAndName(eq(USER_A), argThat("Kniebeuge"::equalsIgnoreCase)))
                .thenReturn(Optional.of(exercise(SQUAT, "Kniebeuge", MuscleGroup.LEGS)));

        // Act & Assert
        assertThatThrownBy(() -> exerciseService.updateExercise(USER_A, BENCH_PRESS,
                new ExerciseRequest("kniebeuge", 7, null)))
                .isInstanceOf(ConflictException.class);

        assertThat(benchPress.getName()).isEqualTo("Bankdrücken");
        assertThat(benchPress.getMuscleGroup()).isEqualTo(MuscleGroup.CHEST);
        verify(exerciseRepository, never()).save(any());
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
        // Arrange
        Exercise benchPress = exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST);
        when(exerciseRepository.findActiveByIdAndUserId(BENCH_PRESS, USER_A)).thenReturn(Optional.of(benchPress));
        when(exerciseUsageChecker.isUsedInActiveRoutine(USER_A, BENCH_PRESS)).thenReturn(false);

        // Act
        exerciseService.deleteExercise(USER_A, BENCH_PRESS);

        // Assert: kein hartes Löschen, damit die Historie vollständig bleibt
        assertThat(benchPress.isDeleted()).isTrue();
        verify(exerciseRepository, never()).delete(any());
        verify(exerciseRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("UT-07 Übung in einer nicht gelöschten Routine ergibt 409")
    void deleteExercise_usedInActiveRoutine_throwsConflict() {
        // Arrange
        Exercise benchPress = exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST);
        when(exerciseRepository.findActiveByIdAndUserId(BENCH_PRESS, USER_A)).thenReturn(Optional.of(benchPress));
        when(exerciseUsageChecker.isUsedInActiveRoutine(USER_A, BENCH_PRESS)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> exerciseService.deleteExercise(USER_A, BENCH_PRESS))
                .isInstanceOf(ConflictException.class);

        assertThat(benchPress.isDeleted()).isFalse();
        verify(exerciseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Übung löschen ergibt 404")
    void deleteExercise_foreignOrDeleted_throwsNotFound() {
        // Arrange
        when(exerciseRepository.findActiveByIdAndUserId(BENCH_PRESS, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> exerciseService.deleteExercise(USER_A, BENCH_PRESS))
                .isInstanceOf(NotFoundException.class);

        verify(exerciseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("UT-02 Eigene aktive Übungen werden für eine Routine akzeptiert")
    void assertActiveExercises_ownActiveExercises_passes() {
        // Arrange
        when(exerciseRepository.findAllByIdInAndUserId(anyCollection(), eq(USER_A))).thenReturn(List.of(
                exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST),
                exercise(SQUAT, "Kniebeuge", MuscleGroup.LEGS)));

        // Act & Assert
        assertThatCode(() -> exerciseService.assertActiveExercises(USER_A, List.of(BENCH_PRESS, SQUAT)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Übung in der Liste ergibt 404")
    void assertActiveExercises_foreignOrDeleted_throwsNotFound() {
        // Arrange: Klimmzug gehört Sportler B, Kniebeuge ist soft-gelöscht
        Exercise deletedSquat = exercise(SQUAT, "Kniebeuge", MuscleGroup.LEGS);
        deletedSquat.setDeleted(true);
        when(exerciseRepository.findAllByIdInAndUserId(anyCollection(), eq(USER_A))).thenReturn(
                List.of(exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST)),
                List.of(exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST), deletedSquat));

        // Act & Assert
        assertThatThrownBy(() -> exerciseService.assertActiveExercises(USER_A, List.of(BENCH_PRESS, PULL_UP)))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> exerciseService.assertActiveExercises(USER_A, List.of(BENCH_PRESS, SQUAT)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("UT-07 Gelöschte Übungen bleiben für Routinen und Historie auflösbar")
    void findByIds_includesDeletedExercises() {
        // Arrange
        Exercise deletedSquat = exercise(SQUAT, "Kniebeuge", MuscleGroup.LEGS);
        deletedSquat.setDeleted(true);
        when(exerciseRepository.findAllByIdInAndUserId(anyCollection(), eq(USER_A))).thenReturn(List.of(
                exercise(BENCH_PRESS, "Bankdrücken", MuscleGroup.CHEST), deletedSquat));

        // Act
        Map<Long, ExerciseResponse> response = exerciseService.findByIds(USER_A, List.of(BENCH_PRESS, SQUAT));

        // Assert (#FA-2.6: die Historie bleibt vollständig)
        assertThat(response).containsOnly(
                Map.entry(BENCH_PRESS, new ExerciseResponse(BENCH_PRESS, "Bankdrücken", 1, "Brust", null)),
                Map.entry(SQUAT, new ExerciseResponse(SQUAT, "Kniebeuge", 7, "Beine", null)));
    }

    private static Exercise exercise(Long id, String name, MuscleGroup muscleGroup) {
        return withId(new Exercise(USER_A, name, muscleGroup, null), id);
    }

}
