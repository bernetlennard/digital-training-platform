package ch.bernet.trainingplatform.persistence.routine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class RoutineTest {

    @Test
    @DisplayName("UT-08 Positionen werden in der Reihenfolge der Liste ab 1 vergeben")
    void replaceExercises_assignsPositionsInOrder() {
        // Arrange
        Routine routine = new Routine(USER_A, "Ganzkörper");

        // Act
        routine.replaceExercises(List.of(SQUAT, BENCH_PRESS, PULL_UP));

        // Assert
        assertThat(routine.getExercises())
                .extracting(RoutineExercise::getExerciseId, RoutineExercise::getPosition)
                .containsExactly(tuple(SQUAT, 1), tuple(BENCH_PRESS, 2), tuple(PULL_UP, 3));
    }

    @Test
    @DisplayName("UT-08 Die alte Übungsliste wird vollständig ersetzt")
    void replaceExercises_replacesPreviousList() {
        // Arrange
        Routine routine = new Routine(USER_A, "Ganzkörper");
        routine.replaceExercises(List.of(SQUAT, BENCH_PRESS, PULL_UP));

        // Act: Reihenfolge geändert und eine Übung entfernt (#FA-3.3, #FA-3.6)
        routine.replaceExercises(List.of(PULL_UP, SQUAT));

        // Assert
        assertThat(routine.getExercises())
                .extracting(RoutineExercise::getExerciseId, RoutineExercise::getPosition)
                .containsExactly(tuple(PULL_UP, 1), tuple(SQUAT, 2));
    }

}
