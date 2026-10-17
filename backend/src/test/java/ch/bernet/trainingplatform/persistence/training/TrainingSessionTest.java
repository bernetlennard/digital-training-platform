package ch.bernet.trainingplatform.persistence.training;

import ch.bernet.trainingplatform.common.ConflictException;
import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.persistence.routine.Routine;
import ch.bernet.trainingplatform.persistence.routine.RoutineExercise;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

// Ein Statuswechsel aus dem falschen Status ergibt 404: Für den Aufruf gibt es keine Einheit in diesem Status
class TrainingSessionTest {

    private static final Instant FINISHED_AT = NOW.plus(Duration.ofHours(1));

    @Test
    @DisplayName("UT-11 Geplante Einheit starten setzt Status aktiv und kopiert die Übungen")
    void start_planned_becomesActiveAndCopiesExercises() {
        // Arrange
        TrainingSession session = new TrainingSession(USER_A, ROUTINE_ID, TODAY);

        // Act
        session.start(NOW, List.of(SQUAT, BENCH_PRESS));

        // Assert
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getStartedAt()).isEqualTo(NOW);
        assertThat(session.getFinishedAt()).isNull();
        assertThat(session.getPlannedDate()).isEqualTo(TODAY);
        assertThat(session.getExercises())
                .extracting(SessionExercise::getExerciseId, SessionExercise::getPosition)
                .containsExactly(tuple(SQUAT, 1), tuple(BENCH_PRESS, 2));
        assertThat(session.getExercises()).allSatisfy(exercise -> assertThat(exercise.getSets()).isEmpty());
    }

    @Test
    @DisplayName("UT-11 Nur eine geplante Einheit kann gestartet werden")
    void start_notPlanned_throwsException() {
        // Arrange
        TrainingSession active = activeSession(BENCH_PRESS);
        TrainingSession completed = completedSession();

        // Act & Assert
        assertThatThrownBy(() -> active.start(FINISHED_AT, List.of(SQUAT)))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> completed.start(FINISHED_AT, List.of(SQUAT)))
                .isInstanceOf(NotFoundException.class);

        assertThat(active.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(active.getStartedAt()).isEqualTo(NOW);
        assertThat(active.getExercises()).extracting(SessionExercise::getExerciseId).containsExactly(BENCH_PRESS);
        assertThat(completed.getStatus()).isEqualTo(SessionStatus.COMPLETED);
    }

    @Test
    @DisplayName("UT-09 Gestartete Einheit behält ihre Übungen, wenn die Routine ändert")
    void start_copiesExercises_routineChangeHasNoEffect() {
        // Arrange
        Routine routine = new Routine(USER_A, "Push");
        routine.replaceExercises(List.of(BENCH_PRESS, SQUAT));
        TrainingSession session = new TrainingSession(USER_A, ROUTINE_ID, TODAY);
        session.start(NOW, routine.getExercises().stream().map(RoutineExercise::getExerciseId).toList());

        // Act: Routine nach dem Start ändern (#FA-3.5)
        routine.replaceExercises(List.of(PULL_UP));

        // Assert
        assertThat(session.getExercises())
                .extracting(SessionExercise::getExerciseId, SessionExercise::getPosition)
                .containsExactly(tuple(BENCH_PRESS, 1), tuple(SQUAT, 2));
    }

    @Test
    @DisplayName("UT-10 Geplante Einheit verschieben ändert Routine und Datum")
    void reschedule_planned_updatesRoutineAndDate() {
        // Arrange
        TrainingSession session = new TrainingSession(USER_A, ROUTINE_ID, TODAY);

        // Act
        session.reschedule(21L, TODAY.plusDays(7));

        // Assert
        assertThat(session.getRoutineId()).isEqualTo(21L);
        assertThat(session.getPlannedDate()).isEqualTo(TODAY.plusDays(7));
        assertThat(session.getStatus()).isEqualTo(SessionStatus.PLANNED);
    }

    @Test
    @DisplayName("UT-10 Nur geplante Einheiten können verschoben werden")
    void reschedule_notPlanned_throwsException() {
        // Arrange
        TrainingSession active = activeSession(BENCH_PRESS);
        TrainingSession completed = completedSession();

        // Act & Assert
        assertThatThrownBy(() -> active.reschedule(21L, TODAY.plusDays(7)))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> completed.reschedule(21L, TODAY.plusDays(7)))
                .isInstanceOf(NotFoundException.class);

        assertThat(active.getRoutineId()).isEqualTo(ROUTINE_ID);
        assertThat(active.getPlannedDate()).isEqualTo(TODAY);
        assertThat(completed.getRoutineId()).isEqualTo(ROUTINE_ID);
    }

    @Test
    @DisplayName("UT-14 Abschliessen setzt den Status und entfernt Übungen ohne Satz")
    void finish_withSets_completesAndRemovesExercisesWithoutSets() {
        // Arrange: nur Bankdrücken und Klimmzug haben einen Satz
        TrainingSession session = activeSession(BENCH_PRESS, SQUAT, PULL_UP);
        addSet(session.getExercises().get(0));
        addSet(session.getExercises().get(2));

        // Act
        session.finish(FINISHED_AT);

        // Assert
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getStartedAt()).isEqualTo(NOW);
        assertThat(session.getFinishedAt()).isEqualTo(FINISHED_AT);
        assertThat(session.getExercises())
                .extracting(SessionExercise::getExerciseId)
                .containsExactly(BENCH_PRESS, PULL_UP);
    }

    @Test
    @DisplayName("UT-14 Abschliessen ohne Satz wird abgewiesen")
    void finish_withoutSets_throwsConflict() {
        // Arrange
        TrainingSession session = new TrainingSession(USER_A, ROUTINE_ID, TODAY);
        session.start(NOW, List.of(BENCH_PRESS, SQUAT));

        // Act & Assert
        assertThatThrownBy(() -> session.finish(FINISHED_AT))
                .isInstanceOf(ConflictException.class);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getFinishedAt()).isNull();
        assertThat(session.getExercises()).hasSize(2);
    }

    @Test
    @DisplayName("UT-14 Nur eine aktive Einheit kann abgeschlossen werden")
    void finish_notActive_throwsNotFound() {
        // Arrange
        TrainingSession planned = new TrainingSession(USER_A, ROUTINE_ID, TODAY);
        TrainingSession completed = completedSession();

        // Act & Assert
        assertThatThrownBy(() -> planned.finish(FINISHED_AT))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> completed.finish(FINISHED_AT.plus(Duration.ofHours(1))))
                .isInstanceOf(NotFoundException.class);

        assertThat(planned.getStatus()).isEqualTo(SessionStatus.PLANNED);
        assertThat(completed.getFinishedAt()).isEqualTo(FINISHED_AT);
    }

    @Test
    @DisplayName("UT-14 Abbrechen setzt eine geplante Einheit zurück")
    void revertToPlanned_removesExercisesAndStartTime() {
        // Arrange
        TrainingSession session = activeSession(BENCH_PRESS, SQUAT);
        addSet(session.getExercises().get(0));

        // Act
        session.revertToPlanned();

        // Assert
        assertThat(session.getStatus()).isEqualTo(SessionStatus.PLANNED);
        assertThat(session.getStartedAt()).isNull();
        assertThat(session.getFinishedAt()).isNull();
        assertThat(session.getExercises()).isEmpty();
        assertThat(session.getRoutineId()).isEqualTo(ROUTINE_ID);
        assertThat(session.getPlannedDate()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("UT-14 Abgebrochene Einheit kann erneut gestartet werden")
    void revertToPlanned_canBeStartedAgain() {
        // Arrange
        TrainingSession session = activeSession(BENCH_PRESS, SQUAT);
        session.revertToPlanned();

        // Act
        session.start(FINISHED_AT, List.of(SQUAT));

        // Assert
        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getStartedAt()).isEqualTo(FINISHED_AT);
        assertThat(session.getExercises())
                .extracting(SessionExercise::getExerciseId, SessionExercise::getPosition)
                .containsExactly(tuple(SQUAT, 1));
    }

    @Test
    @DisplayName("UT-11 Einheit ohne geplantes Datum ist spontan")
    void isSpontaneous_withoutPlannedDate_returnsTrue() {
        assertThat(new TrainingSession(USER_A, ROUTINE_ID, null).isSpontaneous()).isTrue();
        assertThat(new TrainingSession(USER_A, ROUTINE_ID, TODAY).isSpontaneous()).isFalse();
    }

    @Test
    @DisplayName("UT-12 Übung und Satz werden nur innerhalb der Einheit gefunden")
    void getExercise_andGetExerciseOfSet_searchWithinSession() {
        // Arrange
        TrainingSession session = activeSession(BENCH_PRESS, SQUAT);
        SessionExercise benchPress = withId(session.getExercises().get(0), 101L);
        SessionExercise squat = withId(session.getExercises().get(1), 102L);
        squat.getSets().add(withId(new ExerciseSet(1, new BigDecimal("100.00"), 5, 180), 501L));

        // Act & Assert
        assertThat(session.getExercise(101L)).isSameAs(benchPress);
        assertThat(session.getExercise(102L)).isSameAs(squat);
        assertThat(session.getExerciseOfSet(501L)).isSameAs(squat);

        assertThatThrownBy(() -> session.getExercise(999L))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> session.getExerciseOfSet(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("UT-15 Abgeschlossene Einheit kann soft gelöscht werden")
    void markDeleted_completed_setsDeleted() {
        // Arrange
        TrainingSession session = completedSession();

        // Act
        session.markDeleted();

        // Assert
        assertThat(session.isDeleted()).isTrue();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getExercises()).isNotEmpty();
    }

    @Test
    @DisplayName("UT-15 Nur abgeschlossene Einheiten können gelöscht werden")
    void markDeleted_notCompleted_throwsException() {
        // Arrange
        TrainingSession planned = new TrainingSession(USER_A, ROUTINE_ID, TODAY);
        TrainingSession active = activeSession(BENCH_PRESS);

        // Act & Assert
        assertThatThrownBy(planned::markDeleted).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(active::markDeleted).isInstanceOf(NotFoundException.class);

        assertThat(planned.isDeleted()).isFalse();
        assertThat(active.isDeleted()).isFalse();
    }

    // Geplant für heute und um NOW gestartet
    private static TrainingSession activeSession(Long... exerciseIds) {
        TrainingSession session = new TrainingSession(USER_A, ROUTINE_ID, TODAY);
        session.start(NOW, List.of(exerciseIds));
        return session;
    }

    // Um NOW gestartet, mit einem Satz Bankdrücken um FINISHED_AT abgeschlossen
    private static TrainingSession completedSession() {
        TrainingSession session = activeSession(BENCH_PRESS);
        addSet(session.getExercises().getFirst());
        session.finish(FINISHED_AT);
        return session;
    }

    // Satz direkt anhängen, damit die Tests nicht von SessionExercise.addSet abhängen
    private static void addSet(SessionExercise exercise) {
        int setNumber = exercise.getSets().size() + 1;
        exercise.getSets().add(new ExerciseSet(setNumber, new BigDecimal("80.00"), 8, 120));
    }

}
