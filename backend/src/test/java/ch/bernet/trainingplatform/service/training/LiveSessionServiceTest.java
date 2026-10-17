package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.common.exception.ConflictException;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import ch.bernet.trainingplatform.persistence.training.ExerciseSet;
import ch.bernet.trainingplatform.persistence.training.ExerciseSetRepository;
import ch.bernet.trainingplatform.persistence.training.SessionExercise;
import ch.bernet.trainingplatform.persistence.training.SessionStatus;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import ch.bernet.trainingplatform.service.training.dto.MoveSetRequest;
import ch.bernet.trainingplatform.service.training.dto.NoteRequest;
import ch.bernet.trainingplatform.service.training.dto.SessionExerciseResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SetRequest;
import ch.bernet.trainingplatform.service.training.dto.SetResponse;
import ch.bernet.trainingplatform.service.training.dto.SetSuggestion;
import ch.bernet.trainingplatform.service.training.dto.StartSessionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveSessionServiceTest {

    private static final Instant STARTED_AT = NOW.minus(Duration.ofHours(1));

    // Routine «Push» mit Kniebeuge an Position 1 und Bankdrücken an Position 2
    private static final RoutineResponse PUSH = new RoutineResponse(ROUTINE_ID, "Push", List.of(
            new RoutineExerciseResponse(1, SQUAT, "Kniebeuge", "Beine"),
            new RoutineExerciseResponse(2, BENCH_PRESS, "Bankdrücken", "Brust")));

    private static final Map<Long, ExerciseResponse> EXERCISES = Map.of(
            BENCH_PRESS, new ExerciseResponse(BENCH_PRESS, "Bankdrücken", 1, "Brust", null),
            SQUAT, new ExerciseResponse(SQUAT, "Kniebeuge", 7, "Beine", null));

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private ExerciseSetRepository exerciseSetRepository;

    @Mock
    private SessionAccess sessionAccess;

    private LiveSessionService liveSessionService;

    // Einheiten, die der simulierten Datenbank bekannt sind, und die neu gespeicherten
    private final List<TrainingSession> knownSessions = new ArrayList<>();
    private final List<TrainingSession> savedSessions = new ArrayList<>();
    private long nextId = 1000L;

    @BeforeEach
    void setUp() {
        liveSessionService = new LiveSessionService(trainingSessionRepository, exerciseSetRepository, sessionAccess, CLOCK);

        // Die Datenbank vergibt IDs erst beim Speichern bzw. beim Flush. Ohne Flush hätten neue Übungen
        // und Sätze in der Antwort keine ID, und das Frontend könnte sie nicht weiter bearbeiten.
        lenient().when(trainingSessionRepository.save(any(TrainingSession.class))).thenAnswer(this::persist);
        lenient().when(trainingSessionRepository.saveAndFlush(any(TrainingSession.class))).thenAnswer(this::persist);
        lenient().doAnswer(invocation -> flush()).when(trainingSessionRepository).flush();
        lenient().doAnswer(invocation -> flush()).when(exerciseSetRepository).flush();
    }

    @Test
    @DisplayName("UT-11 Geplante Einheit starten kopiert die Übungen der Routine")
    void startPlannedSession_copiesRoutineExercises() {
        // Arrange
        TrainingSession planned = plannedSession(TODAY);
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(planned));
        when(sessionAccess.getActiveRoutine(USER_A, ROUTINE_ID)).thenReturn(PUSH);
        stubNameLookups(false);

        // Act
        SessionResponse response = liveSessionService.startPlannedSession(USER_A, SESSION_ID);

        // Assert: Übungen in der Reihenfolge der Routine (#FA-5.1, #FA-3.5)
        assertThat(planned.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(planned.getStartedAt()).isEqualTo(NOW);
        assertThat(planned.getExercises())
                .extracting(SessionExercise::getExerciseId, SessionExercise::getPosition)
                .containsExactly(tuple(SQUAT, 1), tuple(BENCH_PRESS, 2));

        assertThat(response.id()).isEqualTo(SESSION_ID);
        assertThat(response.routineId()).isEqualTo(ROUTINE_ID);
        assertThat(response.routineName()).isEqualTo("Push");
        assertThat(response.routineDeleted()).isFalse();
        assertThat(response.status()).isEqualTo("aktiv");
        assertThat(response.startedAt()).isEqualTo(NOW);
        assertThat(response.finishedAt()).isNull();
        assertThat(response.exercises())
                .extracting(SessionExerciseResponse::exerciseId, SessionExerciseResponse::exerciseName, SessionExerciseResponse::position)
                .containsExactly(tuple(SQUAT, "Kniebeuge", 1), tuple(BENCH_PRESS, "Bankdrücken", 2));
        // Über diese ID erfasst das Frontend die Sätze
        assertThat(response.exercises()).extracting(SessionExerciseResponse::id).doesNotContainNull();
    }

    @Test
    @DisplayName("UT-02 Fremde oder unbekannte geplante Einheit starten ergibt 404")
    void startPlannedSession_foreignOrUnknown_throwsNotFound() {
        // Arrange
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> liveSessionService.startPlannedSession(USER_A, SESSION_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("UT-11 Zweite aktive Einheit ergibt 409")
    void startSession_otherSessionActive_throwsConflict() {
        // Arrange: eine andere Einheit läuft bereits
        TrainingSession running = activeSession(TODAY, BENCH_PRESS);
        TrainingSession planned = withId(new TrainingSession(USER_A, ROUTINE_ID, TODAY), 31L);
        lenient().when(trainingSessionRepository.findByIdAndUserId(31L, USER_A)).thenReturn(Optional.of(planned));
        lenient().when(sessionAccess.getActiveRoutine(USER_A, ROUTINE_ID)).thenReturn(PUSH);

        // Act & Assert (#FA-5.1)
        assertThatThrownBy(() -> liveSessionService.startPlannedSession(USER_A, 31L))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> liveSessionService.startSpontaneousSession(USER_A, new StartSessionRequest(ROUTINE_ID)))
                .isInstanceOf(ConflictException.class);

        assertThat(planned.getStatus()).isEqualTo(SessionStatus.PLANNED);
        assertThat(planned.getExercises()).isEmpty();
        assertThat(running.getStartedAt()).isEqualTo(STARTED_AT);
        assertThat(savedSessions).isEmpty();
    }

    @Test
    @DisplayName("UT-11 Spontane Einheit wird ohne Datum gestartet")
    void startSpontaneousSession_createsActiveSessionWithoutDate() {
        // Arrange
        when(sessionAccess.getActiveRoutine(USER_A, ROUTINE_ID)).thenReturn(PUSH);
        stubNameLookups(false);

        // Act
        SessionResponse response = liveSessionService.startSpontaneousSession(USER_A, new StartSessionRequest(ROUTINE_ID));

        // Assert
        assertThat(savedSessions).singleElement().satisfies(saved -> {
            assertThat(saved.getUserId()).isEqualTo(USER_A);
            assertThat(saved.getRoutineId()).isEqualTo(ROUTINE_ID);
            assertThat(saved.getPlannedDate()).isNull();
            assertThat(saved.isSpontaneous()).isTrue();
            assertThat(saved.getStatus()).isEqualTo(SessionStatus.ACTIVE);
            assertThat(saved.getStartedAt()).isEqualTo(NOW);
            assertThat(saved.getExercises())
                    .extracting(SessionExercise::getExerciseId, SessionExercise::getPosition)
                    .containsExactly(tuple(SQUAT, 1), tuple(BENCH_PRESS, 2));
        });

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo("aktiv");
        assertThat(response.routineName()).isEqualTo("Push");
        assertThat(response.exercises())
                .extracting(SessionExerciseResponse::exerciseId, SessionExerciseResponse::exerciseName)
                .containsExactly(tuple(SQUAT, "Kniebeuge"), tuple(BENCH_PRESS, "Bankdrücken"));
        assertThat(response.exercises()).extracting(SessionExerciseResponse::id).doesNotContainNull();
    }

    @Test
    @DisplayName("UT-11 Ohne aktive Einheit wird keine Einheit geliefert")
    void getActiveSession_withoutActiveSession_returnsNull() {
        // Arrange
        when(trainingSessionRepository.findByUserIdAndStatus(USER_A, SessionStatus.ACTIVE)).thenReturn(List.of());

        // Act & Assert: der Controller antwortet darauf mit 204
        assertThat(liveSessionService.getActiveSession(USER_A)).isNull();
    }

    @Test
    @DisplayName("UT-11 Aktive Einheit wird mit allen Sätzen und der Notiz geliefert")
    void getActiveSession_returnsSetsAndNote() {
        // Arrange: die Routine wurde nach dem Start gelöscht
        TrainingSession session = activeSession(TODAY, BENCH_PRESS);
        session.setNote("Schulter etwas müde");
        SessionExercise benchPress = session.getExercises().getFirst();
        addSet(benchPress, 501L, "80.00", 8, 120);
        addSet(benchPress, 502L, "82.50", 6, null);
        stubNameLookups(true);

        // Act
        SessionResponse response = liveSessionService.getActiveSession(USER_A);

        // Assert (#FA-5.9)
        assertThat(response.id()).isEqualTo(SESSION_ID);
        assertThat(response.status()).isEqualTo("aktiv");
        assertThat(response.routineName()).isEqualTo("Push");
        assertThat(response.routineDeleted()).isTrue();
        assertThat(response.startedAt()).isEqualTo(STARTED_AT);
        assertThat(response.note()).isEqualTo("Schulter etwas müde");
        assertThat(response.exercises()).singleElement().satisfies(exercise -> {
            assertThat(exercise.id()).isEqualTo(101L);
            assertThat(exercise.exerciseName()).isEqualTo("Bankdrücken");
            assertThat(exercise.sets()).containsExactly(
                    new SetResponse(501L, 1, new BigDecimal("80.00"), 8, 120),
                    new SetResponse(502L, 2, new BigDecimal("82.50"), 6, null));
        });
    }

    @Test
    @DisplayName("UT-13 Vorschlag aus der letzten abgeschlossenen Einheit")
    void suggestion_fromLastCompletedSession() {
        // Arrange: Bankdrücken hat schon einen Satz, Kniebeuge noch keinen
        TrainingSession session = activeSession(TODAY, BENCH_PRESS, SQUAT);
        addSet(session.getExercises().get(0), 501L, "80.00", 8, 120);
        when(exerciseSetRepository.findLastCompletedSets(USER_A, BENCH_PRESS)).thenReturn(List.of(
                set(1, "75.00", 10, 90),
                set(2, "77.50", 8, 90)));
        when(exerciseSetRepository.findLastCompletedSets(USER_A, SQUAT)).thenReturn(List.of(
                set(1, "100.00", 5, 180)));
        stubNameLookups(false);

        // Act
        SessionResponse response = liveSessionService.getActiveSession(USER_A);

        // Assert: jeweils der Satz mit der nächsten Nummer aus der letzten Einheit (#FA-5.5)
        assertThat(response.exercises())
                .extracting(SessionExerciseResponse::exerciseId, SessionExerciseResponse::nextSet)
                .containsExactly(
                        tuple(BENCH_PRESS, new SetSuggestion(new BigDecimal("77.50"), 8, 90)),
                        tuple(SQUAT, new SetSuggestion(new BigDecimal("100.00"), 5, 180)));
    }

    @Test
    @DisplayName("UT-13 Vorschlag aus dem vorherigen Satz der aktuellen Einheit")
    void suggestion_fromPreviousSetOfCurrentSession() {
        // Arrange: die letzte Einheit hatte nur 2 Sätze, jetzt folgt der 3. Satz
        TrainingSession session = activeSession(TODAY, BENCH_PRESS);
        SessionExercise benchPress = session.getExercises().getFirst();
        addSet(benchPress, 501L, "80.00", 8, 120);
        addSet(benchPress, 502L, "82.50", 6, 150);
        when(exerciseSetRepository.findLastCompletedSets(USER_A, BENCH_PRESS)).thenReturn(List.of(
                set(1, "75.00", 10, 90),
                set(2, "77.50", 8, 90)));
        stubNameLookups(false);

        // Act
        SessionResponse response = liveSessionService.getActiveSession(USER_A);

        // Assert
        assertThat(response.exercises().getFirst().nextSet())
                .isEqualTo(new SetSuggestion(new BigDecimal("82.50"), 6, 150));
    }

    @Test
    @DisplayName("UT-13 Ohne frühere Werte gibt es keinen Vorschlag")
    void suggestion_withoutHistory_isEmpty() {
        // Arrange: keine abgeschlossene Einheit mit Bankdrücken und noch kein Satz in der aktuellen
        activeSession(TODAY, BENCH_PRESS);
        when(exerciseSetRepository.findLastCompletedSets(USER_A, BENCH_PRESS)).thenReturn(List.of());
        stubNameLookups(false);

        // Act
        SessionResponse response = liveSessionService.getActiveSession(USER_A);

        // Assert
        assertThat(response.exercises().getFirst().nextSet()).isNull();
    }

    @Test
    @DisplayName("UT-12 Satz erfassen hängt ihn mit ID an die Übung der aktiven Einheit an")
    void addSet_activeSession_appendsSetWithId() {
        // Arrange
        TrainingSession session = activeSession(TODAY, BENCH_PRESS);
        stubNameLookups(false);

        // Act
        SessionExerciseResponse response = liveSessionService.addSet(USER_A, 101L,
                new SetRequest(new BigDecimal("80.00"), 8, 120));

        // Assert
        assertThat(session.getExercises().getFirst().getSets())
                .extracting(ExerciseSet::getSetNumber, ExerciseSet::getWeightKg, ExerciseSet::getRepetitions, ExerciseSet::getRestSeconds)
                .containsExactly(tuple(1, new BigDecimal("80.00"), 8, 120));

        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.exerciseId()).isEqualTo(BENCH_PRESS);
        assertThat(response.exerciseName()).isEqualTo("Bankdrücken");
        assertThat(response.sets()).singleElement().satisfies(set -> {
            assertThat(set.id()).isNotNull();
            assertThat(set.setNumber()).isEqualTo(1);
        });
        // Ohne frühere Einheit wird der eben erfasste Satz vorgeschlagen
        assertThat(response.nextSet()).isEqualTo(new SetSuggestion(new BigDecimal("80.00"), 8, 120));
    }

    @Test
    @DisplayName("UT-12 Satz ändern, verschieben und löschen in der aktiven Einheit")
    void updateMoveAndDeleteSet_activeSession_returnsWholeExercise() {
        // Arrange
        TrainingSession session = activeSession(TODAY, BENCH_PRESS);
        SessionExercise benchPress = session.getExercises().getFirst();
        addSet(benchPress, 501L, "80.00", 8, 120);
        addSet(benchPress, 502L, "80.00", 8, 120);
        addSet(benchPress, 503L, "80.00", 8, 120);
        stubNameLookups(false);

        // Act & Assert: ändern behält die Nummer
        SessionExerciseResponse updated = liveSessionService.updateSet(USER_A, 502L,
                new SetRequest(new BigDecimal("90.00"), 5, 180));
        assertThat(updated.sets()).extracting(SetResponse::id, SetResponse::setNumber, SetResponse::weightKg)
                .containsExactly(
                        tuple(501L, 1, new BigDecimal("80.00")),
                        tuple(502L, 2, new BigDecimal("90.00")),
                        tuple(503L, 3, new BigDecimal("80.00")));

        // Act & Assert: verschieben nummeriert neu (#FA-5.4)
        SessionExerciseResponse moved = liveSessionService.moveSet(USER_A, 503L, new MoveSetRequest(1));
        assertThat(moved.sets()).extracting(SetResponse::id, SetResponse::setNumber)
                .containsExactly(tuple(503L, 1), tuple(501L, 2), tuple(502L, 3));

        // Act & Assert: löschen nummeriert lückenlos
        SessionExerciseResponse deleted = liveSessionService.deleteSet(USER_A, 501L);
        assertThat(deleted.id()).isEqualTo(101L);
        assertThat(deleted.sets()).extracting(SetResponse::id, SetResponse::setNumber)
                .containsExactly(tuple(503L, 1), tuple(502L, 2));
    }

    @Test
    @DisplayName("UT-14 Abschliessen setzt den Abschlusszeitpunkt")
    void finishSession_setsFinishedAt() {
        // Arrange: Kniebeuge wurde ausgelassen
        TrainingSession session = activeSession(TODAY, BENCH_PRESS, SQUAT);
        addSet(session.getExercises().get(0), 501L, "80.00", 8, 120);

        // Act
        liveSessionService.finishSession(USER_A);

        // Assert (#FA-5.7)
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getStartedAt()).isEqualTo(STARTED_AT);
        assertThat(session.getFinishedAt()).isEqualTo(NOW);
        assertThat(session.getExercises()).extracting(SessionExercise::getExerciseId).containsExactly(BENCH_PRESS);
    }

    @Test
    @DisplayName("UT-14 Abbruch einer geplanten Einheit setzt sie zurück")
    void cancelSession_planned_revertsToPlanned() {
        // Arrange
        TrainingSession session = activeSession(TODAY, BENCH_PRESS, SQUAT);
        addSet(session.getExercises().get(0), 501L, "80.00", 8, 120);

        // Act
        liveSessionService.cancelSession(USER_A);

        // Assert (#FA-5.8)
        assertThat(session.getStatus()).isEqualTo(SessionStatus.PLANNED);
        assertThat(session.getStartedAt()).isNull();
        assertThat(session.getExercises()).isEmpty();
        assertThat(session.getPlannedDate()).isEqualTo(TODAY);
        verify(trainingSessionRepository, never()).delete(any());
        verify(trainingSessionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("UT-14 Abbruch einer spontanen Einheit löscht sie")
    void cancelSession_spontaneous_deletesSession() {
        // Arrange
        TrainingSession session = activeSession(null, BENCH_PRESS);
        addSet(session.getExercises().getFirst(), 501L, "80.00", 8, 120);

        // Act
        liveSessionService.cancelSession(USER_A);

        // Assert (#FA-5.8)
        verify(trainingSessionRepository).delete(session);
    }

    @Test
    @DisplayName("UT-14 Ohne aktive Einheit ergeben Notiz, Abschliessen und Abbrechen 404")
    void sessionActions_withoutActiveSession_throwNotFound() {
        // Arrange
        when(trainingSessionRepository.findByUserIdAndStatus(USER_A, SessionStatus.ACTIVE)).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> liveSessionService.updateNote(USER_A, new NoteRequest("Notiz")))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> liveSessionService.finishSession(USER_A))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> liveSessionService.cancelSession(USER_A))
                .isInstanceOf(NotFoundException.class);

        verify(trainingSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("UT-15 Ohne aktive Einheit sind keine Änderungen an Sätzen möglich")
    void setOperations_withoutActiveSession_throwNotFound() {
        // Arrange
        when(trainingSessionRepository.findByUserIdAndStatus(USER_A, SessionStatus.ACTIVE)).thenReturn(List.of());
        SetRequest request = new SetRequest(new BigDecimal("80.00"), 8, 120);

        // Act & Assert
        assertThatThrownBy(() -> liveSessionService.addSet(USER_A, 101L, request))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> liveSessionService.updateSet(USER_A, 501L, request))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> liveSessionService.moveSet(USER_A, 501L, new MoveSetRequest(1)))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> liveSessionService.deleteSet(USER_A, 501L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("UT-15 Sätze einer abgeschlossenen Einheit können nicht mehr bearbeitet werden")
    void setOperations_setOfCompletedSession_throwNotFound() {
        // Arrange: Übung 999 und Satz 777 gehören zu einer abgeschlossenen Einheit (#FA-6.5)
        TrainingSession session = activeSession(TODAY, BENCH_PRESS);
        addSet(session.getExercises().getFirst(), 501L, "80.00", 8, 120);
        SetRequest request = new SetRequest(new BigDecimal("90.00"), 5, 180);

        // Act & Assert
        assertThatThrownBy(() -> liveSessionService.addSet(USER_A, 999L, request))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> liveSessionService.updateSet(USER_A, 777L, request))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> liveSessionService.moveSet(USER_A, 777L, new MoveSetRequest(1)))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> liveSessionService.deleteSet(USER_A, 777L))
                .isInstanceOf(NotFoundException.class);

        assertThat(session.getExercises().getFirst().getSets())
                .extracting(ExerciseSet::getId, ExerciseSet::getWeightKg)
                .containsExactly(tuple(501L, new BigDecimal("80.00")));
    }

    // Noch nicht gestartete Einheit; ohne Datum ist sie spontan
    private TrainingSession plannedSession(LocalDate plannedDate) {
        TrainingSession session = withId(new TrainingSession(USER_A, ROUTINE_ID, plannedDate), SESSION_ID);
        knownSessions.add(session);
        return session;
    }

    // Aktive Einheit, um STARTED_AT gestartet; die Übungen erhalten die IDs 101, 102, ...
    private TrainingSession activeSession(LocalDate plannedDate, Long... exerciseIds) {
        TrainingSession session = plannedSession(plannedDate);
        session.start(STARTED_AT, List.of(exerciseIds));
        for (int i = 0; i < session.getExercises().size(); i++) {
            withId(session.getExercises().get(i), 101L + i);
        }
        when(trainingSessionRepository.findByUserIdAndStatus(USER_A, SessionStatus.ACTIVE)).thenReturn(List.of(session));
        return session;
    }

    // Namen für die Antwort: je nach Umsetzung aus der Routine, den Zusammenfassungen oder den Übungen
    private void stubNameLookups(boolean routineDeleted) {
        lenient().when(sessionAccess.getRoutineSummaries(USER_A))
                .thenReturn(Map.of(ROUTINE_ID, new RoutineSummary(ROUTINE_ID, "Push", routineDeleted)));
        lenient().when(sessionAccess.getExercises(eq(USER_A), anyCollection())).thenReturn(EXERCISES);
    }

    // Satz direkt anhängen, damit die Tests nicht von SessionExercise.addSet abhängen
    private static void addSet(SessionExercise exercise, Long id, String weightKg, int repetitions, Integer restSeconds) {
        int setNumber = exercise.getSets().size() + 1;
        exercise.getSets().add(withId(new ExerciseSet(setNumber, new BigDecimal(weightKg), repetitions, restSeconds), id));
    }

    private static ExerciseSet set(int setNumber, String weightKg, int repetitions, Integer restSeconds) {
        return new ExerciseSet(setNumber, new BigDecimal(weightKg), repetitions, restSeconds);
    }

    private TrainingSession persist(InvocationOnMock invocation) {
        TrainingSession session = invocation.getArgument(0);
        savedSessions.add(session);
        knownSessions.add(session);
        assignMissingIds(session);
        return session;
    }

    private Object flush() {
        knownSessions.forEach(this::assignMissingIds);
        return null;
    }

    private void assignMissingIds(TrainingSession session) {
        if (session.getId() == null) {
            withId(session, nextId++);
        }
        for (SessionExercise exercise : session.getExercises()) {
            if (exercise.getId() == null) {
                withId(exercise, nextId++);
            }
            for (ExerciseSet set : exercise.getSets()) {
                if (set.getId() == null) {
                    withId(set, nextId++);
                }
            }
        }
    }

}
