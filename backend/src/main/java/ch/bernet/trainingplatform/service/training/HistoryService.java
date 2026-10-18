package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.common.exception.NotFoundException;
import ch.bernet.trainingplatform.persistence.training.SessionExercise;
import ch.bernet.trainingplatform.persistence.training.SessionStatus;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import ch.bernet.trainingplatform.service.training.dto.HistoryRoutineResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionExerciseResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class HistoryService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final SessionAccess sessionAccess;

    public HistoryService(TrainingSessionRepository trainingSessionRepository, SessionAccess sessionAccess) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.sessionAccess = sessionAccess;
    }

    @Transactional(readOnly = true)
    public List<HistoryRoutineResponse> getHistoryRoutines(Long userId) {
        // Routinen mit mind. einer abgeschlossenen Einheit (findCompletedRoutineIds), auch gelöschte
        Map<Long, RoutineSummary> routines = sessionAccess.getRoutineSummaries(userId);
        return trainingSessionRepository.findCompletedRoutineIds(userId).stream()
                .map(routines::get)
                .filter(Objects::nonNull)
                .map(routine -> new HistoryRoutineResponse(routine.id(), routine.name(), routine.deleted()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SessionSummaryResponse> getRoutineHistory(Long userId, Long routineId) {
        // abgeschlossene Einheiten der Routine als Zusammenfassung, neueste zuerst
        return trainingSessionRepository.findCompletedByUserIdAndRoutineId(userId, routineId).stream()
                .sorted(Comparator.comparing(TrainingSession::getStartedAt).reversed())
                .map(TrainingMapper::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionDetail(Long userId, Long sessionId) {
        // abgeschlossene Einheit laden (sonst NotFoundException), Übungsnamen auflösen, mappen
        TrainingSession session = trainingSessionRepository.findByIdAndUserId(sessionId, userId)
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED && !s.isDeleted())
                .orElseThrow(() -> new NotFoundException("Session not found"));
        RoutineSummary routine = sessionAccess.getRoutineSummaries(userId).get(session.getRoutineId());
        List<Long> exerciseIds = session.getExercises().stream().map(SessionExercise::getExerciseId).toList();
        Map<Long, ExerciseResponse> names = sessionAccess.getExercises(userId, exerciseIds);
        // Im Verlauf gibt es keinen Vorschlag für den nächsten Satz
        List<SessionExerciseResponse> exercises = session.getExercises().stream()
                .map(item -> TrainingMapper.toExercise(item, names.get(item.getExerciseId()).name(), null))
                .toList();
        return TrainingMapper.toSession(session, routine, exercises);
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        // Einheit laden (sonst NotFoundException), markDeleted (#FA-6.5)
        trainingSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Session not found"))
                .markDeleted();
    }

}
