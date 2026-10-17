package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.bernet.trainingplatform.TestData.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PlannedSessionCleanerTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private PlannedSessionCleaner plannedSessionCleaner;

    @Test
    @DisplayName("UT-09 Geplante Einheiten einer gelöschten Routine werden hart gelöscht")
    void onRoutineDeleted_deletesPlannedSessions() {
        // Act
        plannedSessionCleaner.onRoutineDeleted(USER_A, ROUTINE_ID);

        // Assert: aktive und abgeschlossene Einheiten bleiben, das filtert die Abfrage (#FA-3.7)
        verify(trainingSessionRepository).deletePlannedByRoutineId(ROUTINE_ID);
        verifyNoMoreInteractions(trainingSessionRepository);
    }

}
