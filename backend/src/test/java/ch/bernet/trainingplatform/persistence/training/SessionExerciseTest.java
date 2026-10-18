package ch.bernet.trainingplatform.persistence.training;

import ch.bernet.trainingplatform.common.exception.BusinessRuleException;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class SessionExerciseTest {

    @Test
    @DisplayName("UT-12 Sätze werden fortlaufend nummeriert")
    void addSet_numbersSetsSequentially() {
        // Arrange
        SessionExercise exercise = new SessionExercise(BENCH_PRESS, 1);

        // Act
        exercise.addSet(new BigDecimal("80.00"), 8, 120);
        exercise.addSet(new BigDecimal("82.50"), 6, null);

        // Assert
        assertThat(exercise.getSets())
                .extracting(ExerciseSet::getSetNumber, ExerciseSet::getWeightKg,
                        ExerciseSet::getRepetitions, ExerciseSet::getRestSeconds)
                .containsExactly(
                        tuple(1, new BigDecimal("80.00"), 8, 120),
                        tuple(2, new BigDecimal("82.50"), 6, null));
    }

    @Test
    @DisplayName("UT-12 Satz ändern behält die Satznummer")
    void updateSet_replacesValuesAndKeepsNumber() {
        // Arrange
        SessionExercise exercise = exerciseWithSets(3);

        // Act
        exercise.updateSet(502L, new BigDecimal("90.00"), 5, 180);

        // Assert
        ExerciseSet updated = exercise.getSets().get(1);
        assertThat(updated.getId()).isEqualTo(502L);
        assertThat(updated.getSetNumber()).isEqualTo(2);
        assertThat(updated.getWeightKg()).isEqualTo(new BigDecimal("90.00"));
        assertThat(updated.getRepetitions()).isEqualTo(5);
        assertThat(updated.getRestSeconds()).isEqualTo(180);
    }

    @Test
    @DisplayName("UT-12 Nach dem Löschen sind die Satznummern lückenlos")
    void removeSet_renumbersWithoutGaps() {
        // Arrange
        SessionExercise exercise = exerciseWithSets(3);

        // Act: mittleren Satz löschen
        exercise.removeSet(502L);

        // Assert
        assertThat(exercise.getSets())
                .extracting(ExerciseSet::getId, ExerciseSet::getSetNumber)
                .containsExactly(tuple(501L, 1), tuple(503L, 2));
    }

    @Test
    @DisplayName("UT-12 Verschieben ordnet die Sätze neu und nummeriert lückenlos")
    void moveSet_reordersAndRenumbers() {
        // Arrange
        SessionExercise exercise = exerciseWithSets(3);

        // Act: dritten Satz an die erste Stelle verschieben
        exercise.moveSet(503L, 1);

        // Assert
        assertThat(exercise.getSets())
                .extracting(ExerciseSet::getId, ExerciseSet::getSetNumber)
                .containsExactly(tuple(503L, 1), tuple(501L, 2), tuple(502L, 3));

        // Act: ersten Satz ans Ende verschieben
        exercise.moveSet(503L, 3);

        // Assert
        assertThat(exercise.getSets())
                .extracting(ExerciseSet::getId, ExerciseSet::getSetNumber)
                .containsExactly(tuple(501L, 1), tuple(502L, 2), tuple(503L, 3));
    }

    @Test
    @DisplayName("UT-12 Verschieben ausserhalb der vorhandenen Sätze ergibt 422")
    void moveSet_outOfRange_throwsBusinessRule() {
        // Arrange
        SessionExercise exercise = exerciseWithSets(3);

        // Act & Assert
        assertThatThrownBy(() -> exercise.moveSet(501L, 4))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(exercise.getSets())
                .extracting(ExerciseSet::getId, ExerciseSet::getSetNumber)
                .containsExactly(tuple(501L, 1), tuple(502L, 2), tuple(503L, 3));
    }

    @Test
    @DisplayName("UT-12 Unbekannter Satz ergibt 404")
    void updateOrRemoveSet_unknownId_throwsNotFound() {
        // Arrange
        SessionExercise exercise = exerciseWithSets(2);

        // Act & Assert
        assertThatThrownBy(() -> exercise.updateSet(999L, new BigDecimal("80.00"), 8, null))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> exercise.moveSet(999L, 1))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> exercise.removeSet(999L))
                .isInstanceOf(NotFoundException.class);

        assertThat(exercise.getSets()).hasSize(2);
    }

    // Übung mit Sätzen 1..count und den IDs 501, 502, ...
    private static SessionExercise exerciseWithSets(int count) {
        SessionExercise exercise = new SessionExercise(BENCH_PRESS, 1);
        for (int i = 1; i <= count; i++) {
            exercise.getSets().add(withId(new ExerciseSet(i, new BigDecimal("80.00"), 8, 120), 500L + i));
        }
        return exercise;
    }

}
