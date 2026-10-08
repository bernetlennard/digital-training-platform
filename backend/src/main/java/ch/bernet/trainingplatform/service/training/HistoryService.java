package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.training.dto.HistoryRoutineResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        // TODO: Routinen mit mind. einer abgeschlossenen Einheit (findCompletedRoutineIds), auch gelöschte
        return null;
    }

    @Transactional(readOnly = true)
    public List<SessionSummaryResponse> getRoutineHistory(Long userId, Long routineId) {
        // TODO: abgeschlossene Einheiten der Routine als Zusammenfassung
        return null;
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionDetail(Long userId, Long sessionId) {
        // TODO: abgeschlossene Einheit laden (sonst NotFoundException), Übungsnamen auflösen, mappen
        return null;
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        // TODO: Einheit laden (sonst NotFoundException), markDeleted (#FA-6.5)
    }

}
