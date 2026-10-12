package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.bernet.trainingplatform.TestData.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private SessionAccess sessionAccess;

    @InjectMocks
    private HistoryService historyService;

    @Test
    @DisplayName("UT-15 Routinen mit abgeschlossenen Einheiten inklusive gelöschter Routinen")
    void getHistoryRoutines_includesDeletedRoutines() {
    }

    @Test
    @DisplayName("UT-15 Einheiten einer Routine, neueste zuerst")
    void getRoutineHistory_newestFirst() {
    }

    @Test
    @DisplayName("UT-15 Nicht abgeschlossene oder gelöschte Einheit ergibt 404")
    void getSessionDetail_notCompletedOrDeleted_throwsNotFound() {
    }

    @Test
    @DisplayName("UT-15 Abgeschlossene Einheit wird soft gelöscht")
    void deleteSession_completed_softDeletes() {
    }

}
