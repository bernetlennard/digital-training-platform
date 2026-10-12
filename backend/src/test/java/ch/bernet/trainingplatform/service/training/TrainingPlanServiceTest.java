package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.bernet.trainingplatform.TestData.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private SessionAccess sessionAccess;

    private TrainingPlanService trainingPlanService;

    @BeforeEach
    void setUp() {
        trainingPlanService = new TrainingPlanService(trainingSessionRepository, sessionAccess, CLOCK);
    }

    @Test
    @DisplayName("UT-10 Einplanen ab heute speichert eine geplante Einheit")
    void planSession_todayOrLater_savesPlannedSession() {
    }

    @Test
    @DisplayName("UT-10 Einplanen in der Vergangenheit ergibt 422")
    void planSession_inPast_throwsBusinessRule() {
    }

    @Test
    @DisplayName("UT-10 Heute richtet sich nach der Zeitzone Europe/Zurich")
    void planSession_usesZurichTimeZone() {
    }

    @Test
    @DisplayName("UT-10 Überfällige Einheit kann nicht mehr geändert werden")
    void updatePlannedSession_overdue_throwsBusinessRule() {
    }

    @Test
    @DisplayName("UT-10 Nur geplante Einheiten können geändert werden")
    void updatePlannedSession_notPlanned_throwsNotFound() {
    }

    @Test
    @DisplayName("UT-10 Geplante Einheit wird hart gelöscht")
    void deletePlannedSession_planned_deletesSession() {
    }

}
