package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.persistence.training.ExerciseSet;
import ch.bernet.trainingplatform.persistence.training.SessionExercise;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import ch.bernet.trainingplatform.service.training.dto.HistoryRoutineResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionExerciseResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionSummaryResponse;
import ch.bernet.trainingplatform.service.training.dto.SetResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    private static final Instant OCTOBER_1 = Instant.parse("2026-10-01T16:00:00Z");
    private static final Instant OCTOBER_8 = Instant.parse("2026-10-08T16:00:00Z");
    private static final Duration TRAINING_TIME = Duration.ofMinutes(75);

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private SessionAccess sessionAccess;

    @InjectMocks
    private HistoryService historyService;

    @Test
    @DisplayName("UT-15 Routinen mit abgeschlossenen Einheiten inklusive gelöschter Routinen")
    void getHistoryRoutines_includesDeletedRoutines() {
        // Arrange: «Pull» hat noch keine abgeschlossene Einheit, «Beine» ist gelöscht
        when(trainingSessionRepository.findCompletedRoutineIds(USER_A)).thenReturn(Set.of(ROUTINE_ID, 21L));
        when(sessionAccess.getRoutineSummaries(USER_A)).thenReturn(Map.of(
                ROUTINE_ID, new RoutineSummary(ROUTINE_ID, "Push", false),
                21L, new RoutineSummary(21L, "Beine", true),
                22L, new RoutineSummary(22L, "Pull", false)));

        // Act
        List<HistoryRoutineResponse> response = historyService.getHistoryRoutines(USER_A);

        // Assert (#FA-6.3)
        assertThat(response).containsExactlyInAnyOrder(
                new HistoryRoutineResponse(ROUTINE_ID, "Push", false),
                new HistoryRoutineResponse(21L, "Beine", true));
    }

    @Test
    @DisplayName("UT-15 Einheiten einer Routine, neueste zuerst")
    void getRoutineHistory_newestFirst() {
        // Arrange
        when(trainingSessionRepository.findCompletedByUserIdAndRoutineId(USER_A, ROUTINE_ID)).thenReturn(List.of(
                completedSession(31L, OCTOBER_1),
                completedSession(32L, OCTOBER_8)));

        // Act
        List<SessionSummaryResponse> response = historyService.getRoutineHistory(USER_A, ROUTINE_ID);

        // Assert (#FA-6.1)
        assertThat(response).containsExactly(
                new SessionSummaryResponse(32L, OCTOBER_8, OCTOBER_8.plus(TRAINING_TIME)),
                new SessionSummaryResponse(31L, OCTOBER_1, OCTOBER_1.plus(TRAINING_TIME)));
    }

    @Test
    @DisplayName("UT-15 Detail einer abgeschlossenen Einheit, auch für gelöschte Routinen")
    void getSessionDetail_completed_returnsExercisesSetsAndNote() {
        // Arrange: Routine und Kniebeuge wurden nach dem Training gelöscht
        TrainingSession session = completedSession(SESSION_ID, OCTOBER_8);
        session.setNote("Neuer Rekord");
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));
        when(sessionAccess.getRoutineSummaries(USER_A)).thenReturn(Map.of(
                ROUTINE_ID, new RoutineSummary(ROUTINE_ID, "Push", true)));
        when(sessionAccess.getExercises(eq(USER_A), anyCollection())).thenReturn(Map.of(
                BENCH_PRESS, new ExerciseResponse(BENCH_PRESS, "Bankdrücken", 1, "Brust", null),
                SQUAT, new ExerciseResponse(SQUAT, "Kniebeuge", 7, "Beine", null)));

        // Act
        SessionResponse response = historyService.getSessionDetail(USER_A, SESSION_ID);

        // Assert (#FA-6.2)
        assertThat(response.id()).isEqualTo(SESSION_ID);
        assertThat(response.routineName()).isEqualTo("Push");
        assertThat(response.routineDeleted()).isTrue();
        assertThat(response.status()).isEqualTo("abgeschlossen");
        assertThat(response.startedAt()).isEqualTo(OCTOBER_8);
        assertThat(response.finishedAt()).isEqualTo(OCTOBER_8.plus(TRAINING_TIME));
        assertThat(response.note()).isEqualTo("Neuer Rekord");
        assertThat(response.exercises())
                .extracting(SessionExerciseResponse::exerciseName, SessionExerciseResponse::position, SessionExerciseResponse::sets)
                .containsExactly(
                        tuple("Bankdrücken", 1, List.of(new SetResponse(501L, 1, new BigDecimal("80.00"), 8, 120))),
                        tuple("Kniebeuge", 2, List.of(new SetResponse(502L, 1, new BigDecimal("100.00"), 5, 180))));
        // Im Verlauf gibt es keinen Vorschlag für einen nächsten Satz
        assertThat(response.exercises()).extracting(SessionExerciseResponse::nextSet).containsOnlyNulls();
    }

    @Test
    @DisplayName("UT-15 Nicht abgeschlossene oder gelöschte Einheit ergibt 404")
    void getSessionDetail_notCompletedOrDeleted_throwsNotFound() {
        // Arrange: 31 läuft noch, 32 ist aus dem Verlauf gelöscht, 99 gehört Sportler B oder existiert nicht
        TrainingSession active = withId(new TrainingSession(USER_A, ROUTINE_ID, TODAY), 31L);
        active.start(NOW, List.of(BENCH_PRESS));
        TrainingSession deleted = completedSession(32L, OCTOBER_1);
        deleted.markDeleted();
        when(trainingSessionRepository.findByIdAndUserId(31L, USER_A)).thenReturn(Optional.of(active));
        when(trainingSessionRepository.findByIdAndUserId(32L, USER_A)).thenReturn(Optional.of(deleted));
        when(trainingSessionRepository.findByIdAndUserId(99L, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> historyService.getSessionDetail(USER_A, 31L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> historyService.getSessionDetail(USER_A, 32L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> historyService.getSessionDetail(USER_A, 99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("UT-15 Abgeschlossene Einheit wird soft gelöscht")
    void deleteSession_completed_softDeletes() {
        // Arrange
        TrainingSession session = completedSession(SESSION_ID, OCTOBER_8);
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));

        // Act
        historyService.deleteSession(USER_A, SESSION_ID);

        // Assert (#FA-6.4)
        assertThat(session.isDeleted()).isTrue();
        verify(trainingSessionRepository, never()).delete(any());
        verify(trainingSessionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("UT-15 Nicht abgeschlossene oder bereits gelöschte Einheit löschen ergibt 404")
    void deleteSession_notCompletedOrDeleted_throwsNotFound() {
        // Arrange: eine aktive Einheit kann nur abgebrochen werden
        TrainingSession active = withId(new TrainingSession(USER_A, ROUTINE_ID, TODAY), 31L);
        active.start(NOW, List.of(BENCH_PRESS));
        TrainingSession deleted = completedSession(32L, OCTOBER_1);
        deleted.markDeleted();
        when(trainingSessionRepository.findByIdAndUserId(31L, USER_A)).thenReturn(Optional.of(active));
        when(trainingSessionRepository.findByIdAndUserId(32L, USER_A)).thenReturn(Optional.of(deleted));
        when(trainingSessionRepository.findByIdAndUserId(99L, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> historyService.deleteSession(USER_A, 31L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> historyService.deleteSession(USER_A, 32L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> historyService.deleteSession(USER_A, 99L)).isInstanceOf(NotFoundException.class);

        assertThat(active.isDeleted()).isFalse();
        verify(trainingSessionRepository, never()).delete(any());
    }

    // Abgeschlossene Einheit: Bankdrücken (Satz 501) und Kniebeuge (Satz 502), 75 Minuten nach dem Start beendet
    private static TrainingSession completedSession(Long id, Instant startedAt) {
        TrainingSession session = withId(new TrainingSession(USER_A, ROUTINE_ID, null), id);
        session.start(startedAt, List.of(BENCH_PRESS, SQUAT));
        addSet(session.getExercises().get(0), 501L, "80.00", 8, 120);
        addSet(session.getExercises().get(1), 502L, "100.00", 5, 180);
        session.finish(startedAt.plus(TRAINING_TIME));
        return session;
    }

    private static void addSet(SessionExercise exercise, Long id, String weightKg, int repetitions, Integer restSeconds) {
        int setNumber = exercise.getSets().size() + 1;
        exercise.getSets().add(withId(new ExerciseSet(setNumber, new BigDecimal(weightKg), repetitions, restSeconds), id));
    }

}
