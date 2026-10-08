package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.training.dto.PlanSessionRequest;
import ch.bernet.trainingplatform.service.training.dto.PlannedSessionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class TrainingPlanService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final SessionAccess sessionAccess;
    private final Clock clock;

    public TrainingPlanService(TrainingSessionRepository trainingSessionRepository,
                               SessionAccess sessionAccess,
                               Clock clock) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.sessionAccess = sessionAccess;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PlannedSessionResponse> getPlannedSessions(Long userId) {
        // TODO: Einheiten im Status PLANNED laden, Routinenname auflösen, nach Datum sortieren
        return null;
    }

    @Transactional
    public PlannedSessionResponse planSession(Long userId, PlanSessionRequest request) {
        // TODO: assertNotInPast, Routine prüfen (getActiveRoutine), TrainingSession anlegen und speichern
        return null;
    }

    @Transactional
    public PlannedSessionResponse updatePlannedSession(Long userId, Long sessionId, PlanSessionRequest request) {
        // TODO: Einheit laden (sonst NotFoundException), assertNotInPast, Routine prüfen, reschedule
        return null;
    }

    @Transactional
    public void deletePlannedSession(Long userId, Long sessionId) {
        // TODO: geplante Einheit laden (sonst NotFoundException) und hart löschen
    }

    private void assertNotInPast(LocalDate date) {
        // TODO: Datum vor heute (clock) -> BusinessRuleException (#FA-4.2)
    }

}
