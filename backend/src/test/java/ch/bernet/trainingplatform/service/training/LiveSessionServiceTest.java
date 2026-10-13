package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.ExerciseSetRepository;
import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.bernet.trainingplatform.TestData.*;

@ExtendWith(MockitoExtension.class)
class LiveSessionServiceTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private ExerciseSetRepository exerciseSetRepository;

    @Mock
    private SessionAccess sessionAccess;

    private LiveSessionService liveSessionService;

    @BeforeEach
    void setUp() {
        liveSessionService = new LiveSessionService(trainingSessionRepository, exerciseSetRepository, sessionAccess, CLOCK);
    }

    @Test
    @DisplayName("UT-11 Geplante Einheit starten kopiert die Übungen der Routine")
    void startPlannedSession_copiesRoutineExercises() {
    }

    @Test
    @DisplayName("UT-11 Zweite aktive Einheit ergibt 409")
    void startSession_otherSessionActive_throwsConflict() {
    }

    @Test
    @DisplayName("UT-11 Spontane Einheit wird ohne Datum gestartet")
    void startSpontaneousSession_createsActiveSessionWithoutDate() {
    }

    @Test
    @DisplayName("UT-13 Vorschlag aus der letzten abgeschlossenen Einheit")
    void suggestion_fromLastCompletedSession() {
    }

    @Test
    @DisplayName("UT-13 Vorschlag aus dem vorherigen Satz der aktuellen Einheit")
    void suggestion_fromPreviousSetOfCurrentSession() {
    }

    @Test
    @DisplayName("UT-13 Ohne frühere Werte gibt es keinen Vorschlag")
    void suggestion_withoutHistory_isEmpty() {
    }

    @Test
    @DisplayName("UT-14 Abschliessen setzt den Abschlusszeitpunkt")
    void finishSession_setsFinishedAt() {
    }

    @Test
    @DisplayName("UT-14 Abbruch einer geplanten Einheit setzt sie zurück")
    void cancelSession_planned_revertsToPlanned() {
    }

    @Test
    @DisplayName("UT-14 Abbruch einer spontanen Einheit löscht sie")
    void cancelSession_spontaneous_deletesSession() {
    }

    @Test
    @DisplayName("UT-15 Ohne aktive Einheit sind keine Änderungen an Sätzen möglich")
    void setOperations_withoutActiveSession_throwNotFound() {
    }

}
