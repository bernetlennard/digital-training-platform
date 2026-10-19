package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.common.exception.BusinessRuleException;
import ch.bernet.trainingplatform.common.exception.ErrorMessages;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import ch.bernet.trainingplatform.persistence.training.SessionStatus;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import ch.bernet.trainingplatform.service.training.dto.PlanSessionRequest;
import ch.bernet.trainingplatform.service.training.dto.PlannedSessionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class TrainingPlanService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final SessionAccess sessionAccess;
    private final Clock clock;

    public TrainingPlanService(TrainingSessionRepository trainingSessionRepository, SessionAccess sessionAccess, Clock clock) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.sessionAccess = sessionAccess;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PlannedSessionResponse> getPlannedSessions(Long userId) {
        // Einheiten im Status PLANNED laden, Routinenname auflösen, nach Datum sortieren
        Map<Long, RoutineSummary> routines = sessionAccess.getRoutineSummaries(userId);
        return trainingSessionRepository.findByUserIdAndStatus(userId, SessionStatus.PLANNED).stream()
                .sorted(Comparator.comparing(TrainingSession::getPlannedDate))
                .map(session -> TrainingMapper.toPlanned(session, routines.get(session.getRoutineId()).name()))
                .toList();
    }

    @Transactional
    public PlannedSessionResponse planSession(Long userId, PlanSessionRequest request) {
        // assertNotInPast, Routine prüfen (getActiveRoutine), TrainingSession anlegen und speichern
        assertNotInPast(request.plannedDate());
        RoutineResponse routineResponse = sessionAccess.getActiveRoutine(userId, request.routineId());
        TrainingSession session = new TrainingSession(userId, request.routineId(), request.plannedDate());
        trainingSessionRepository.save(session);
        return TrainingMapper.toPlanned(session, routineResponse.name());
    }

    @Transactional
    public PlannedSessionResponse updatePlannedSession(Long userId, Long sessionId, PlanSessionRequest request) {
        // Einheit laden (sonst NotFoundException), assertNotInPast, Routine prüfen, reschedule
        TrainingSession session = trainingSessionRepository.findByIdAndUserId(sessionId, userId)
                .filter(s -> s.getStatus() == SessionStatus.PLANNED)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.SESSION_NOT_FOUND));
        assertNotInPast(session.getPlannedDate());
        assertNotInPast(request.plannedDate());
        RoutineResponse routineResponse = sessionAccess.getActiveRoutine(userId, request.routineId());
        session.reschedule(request.routineId(), request.plannedDate());
        return TrainingMapper.toPlanned(session, routineResponse.name());
    }

    @Transactional
    public void deletePlannedSession(Long userId, Long sessionId) {
        // geplante Einheit laden (sonst NotFoundException) und hart löschen
        TrainingSession session = trainingSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.SESSION_NOT_FOUND));
        if (session.getStatus() != SessionStatus.PLANNED) {
            throw new NotFoundException(ErrorMessages.SESSION_NOT_PLANNED);
        }
        trainingSessionRepository.delete(session);
    }

    private void assertNotInPast(LocalDate date) {
        // Datum vor heute (clock) -> BusinessRuleException
        if (date.isBefore(LocalDate.now(clock))) {
            throw new BusinessRuleException(ErrorMessages.PLANNED_DATE_IN_PAST);
        }
    }

}
