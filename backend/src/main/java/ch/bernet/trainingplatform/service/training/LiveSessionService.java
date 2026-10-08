package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.ExerciseSetRepository;
import ch.bernet.trainingplatform.persistence.training.SessionExercise;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.training.dto.MoveSetRequest;
import ch.bernet.trainingplatform.service.training.dto.NoteRequest;
import ch.bernet.trainingplatform.service.training.dto.SessionExerciseResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SetRequest;
import ch.bernet.trainingplatform.service.training.dto.SetSuggestion;
import ch.bernet.trainingplatform.service.training.dto.StartSessionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class LiveSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final SessionAccess sessionAccess;
    private final Clock clock;

    public LiveSessionService(TrainingSessionRepository trainingSessionRepository,
                              ExerciseSetRepository exerciseSetRepository,
                              SessionAccess sessionAccess,
                              Clock clock) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.sessionAccess = sessionAccess;
        this.clock = clock;
    }

    @Transactional
    public SessionResponse startPlannedSession(Long userId, Long sessionId) {
        // TODO: schon eine aktive Einheit -> ConflictException (#FA-5.1), geplante Einheit laden, start mit Übungen der Routine
        return null;
    }

    @Transactional
    public SessionResponse startSpontaneousSession(Long userId, StartSessionRequest request) {
        // TODO: schon eine aktive Einheit -> ConflictException (#FA-5.1), Einheit ohne Datum anlegen und starten
        return null;
    }

    @Transactional(readOnly = true)
    public SessionResponse getActiveSession(Long userId) {
        // TODO: aktive Einheit oder null zurückgeben, inkl. Vorschlag pro Übung (suggestNextSet)
        return null;
    }

    @Transactional
    public void updateNote(Long userId, NoteRequest request) {
        // TODO: Notiz der aktiven Einheit setzen
    }

    @Transactional
    public SessionExerciseResponse addSet(Long userId, Long sessionExerciseId, SetRequest request) {
        // TODO: getActiveOrThrow, getExercise, addSet
        return null;
    }

    @Transactional
    public SessionExerciseResponse updateSet(Long userId, Long setId, SetRequest request) {
        // TODO: getActiveOrThrow, getExerciseOfSet, updateSet
        return null;
    }

    @Transactional
    public SessionExerciseResponse moveSet(Long userId, Long setId, MoveSetRequest request) {
        // TODO: getActiveOrThrow, getExerciseOfSet, moveSet (#FA-5.4)
        return null;
    }

    @Transactional
    public SessionExerciseResponse deleteSet(Long userId, Long setId) {
        // TODO: getActiveOrThrow, getExerciseOfSet, removeSet
        return null;
    }

    @Transactional
    public void finishSession(Long userId) {
        // TODO: getActiveOrThrow, finish mit clock.instant() (#FA-5.7)
    }

    @Transactional
    public void cancelSession(Long userId) {
        // TODO: geplante Einheit -> revertToPlanned, spontane Einheit -> hart löschen (#FA-5.8)
    }

    private TrainingSession getActiveOrThrow(Long userId) {
        // TODO: aktive Einheit laden, sonst NotFoundException
        return null;
    }

    private SetSuggestion suggestNextSet(SessionExercise item, Long userId) {
        // TODO: Vorschlag aus findLastCompletedSets für den nächsten Satz, null wenn keiner (#FA-5.5)
        return null;
    }

}
