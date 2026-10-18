package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.common.exception.ConflictException;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import ch.bernet.trainingplatform.persistence.training.*;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import ch.bernet.trainingplatform.service.training.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Map;

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
        // schon eine aktive Einheit -> ConflictException (#FA-5.1), geplante Einheit laden, start mit Übungen der Routine
        if (!trainingSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE).isEmpty()) {
            throw new ConflictException("A different Session has already been started");
        }
        TrainingSession session = trainingSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Session Not Found"));
        RoutineResponse routineResponse = sessionAccess.getActiveRoutine(userId, session.getRoutineId());
        List<Long> exerciseIds = routineResponse.exercises().stream().map(RoutineExerciseResponse::exerciseId).toList();
        session.start(clock.instant(), exerciseIds);
        trainingSessionRepository.flush();
        return toResponse(userId, session);
    }

    @Transactional
    public SessionResponse startSpontaneousSession(Long userId, StartSessionRequest request) {
        // schon eine aktive Einheit -> ConflictException (#FA-5.1), Einheit ohne Datum anlegen und starten
        if (!trainingSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE).isEmpty()) {
            throw new ConflictException("A different Session has already been started");
        }

        TrainingSession session = new TrainingSession(userId, request.routineId(), null);
        RoutineResponse routineResponse = sessionAccess.getActiveRoutine(userId, request.routineId());
        List<Long> exerciseIds = routineResponse.exercises().stream().map(RoutineExerciseResponse::exerciseId).toList();
        session.start(clock.instant(), exerciseIds);
        trainingSessionRepository.saveAndFlush(session);
        return toResponse(userId, session);
    }

    @Transactional(readOnly = true)
    public SessionResponse getActiveSession(Long userId) {
        // aktive Einheit oder null zurückgeben, inkl. Vorschlag pro Übung (suggestNextSet)
        return trainingSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE).stream()
                .findFirst().map(s -> toResponse(userId, s)).orElse(null);
    }

    @Transactional
    public void updateNote(Long userId, NoteRequest request) {
        // Notiz der aktiven Einheit setzen
        getActiveOrThrow(userId).setNote(request.note());
    }

    @Transactional
    public SessionExerciseResponse addSet(Long userId, Long sessionExerciseId, SetRequest request) {
        // getActiveOrThrow, getExercise, addSet; flush, damit der neue Satz eine ID hat
        SessionExercise item = getActiveOrThrow(userId).getExercise(sessionExerciseId);
        item.addSet(request.weightKg(), request.repetitions(), request.restSeconds());
        trainingSessionRepository.flush();
        return toResponse(userId, item);
    }

    @Transactional
    public SessionExerciseResponse updateSet(Long userId, Long setId, SetRequest request) {
        // getActiveOrThrow, getExerciseOfSet, updateSet
        SessionExercise item = getActiveOrThrow(userId).getExerciseOfSet(setId);
        item.updateSet(setId, request.weightKg(), request.repetitions(), request.restSeconds());
        return toResponse(userId, item);
    }

    @Transactional
    public SessionExerciseResponse moveSet(Long userId, Long setId, MoveSetRequest request) {
        // getActiveOrThrow, getExerciseOfSet, moveSet (#FA-5.4)
        SessionExercise item = getActiveOrThrow(userId).getExerciseOfSet(setId);
        item.moveSet(setId, request.newSetNumber());
        return toResponse(userId, item);
    }

    @Transactional
    public SessionExerciseResponse deleteSet(Long userId, Long setId) {
        // getActiveOrThrow, getExerciseOfSet, removeSet
        SessionExercise item = getActiveOrThrow(userId).getExerciseOfSet(setId);
        item.removeSet(setId);
        return toResponse(userId, item);
    }

    @Transactional
    public void finishSession(Long userId) {
        // getActiveOrThrow, finish mit clock.instant() (#FA-5.7)
        getActiveOrThrow(userId).finish(clock.instant());
    }

    @Transactional
    public void cancelSession(Long userId) {
        // geplante Einheit -> revertToPlanned, spontane Einheit -> hart löschen (#FA-5.8)
        TrainingSession session = getActiveOrThrow(userId);
        if (session.isSpontaneous()) {
            trainingSessionRepository.delete(session);
        } else {
            session.revertToPlanned();
        }
    }

    private TrainingSession getActiveOrThrow(Long userId) {
        // aktive Einheit laden, sonst NotFoundException
        return trainingSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No active session found"));
    }

    private SetSuggestion suggestNextSet(SessionExercise item, Long userId) {
        // Vorschlag aus findLastCompletedSets für den nächsten Satz, null wenn keiner (#FA-5.5)
        int nextSetNumber = item.getSets().size() + 1;
        ExerciseSet source = exerciseSetRepository.findLastCompletedSets(userId, item.getExerciseId()).stream()
                .filter(set -> set.getSetNumber() == nextSetNumber)
                .findFirst()
                // sonst den vorherigen Satz der aktuellen Einheit wiederholen
                .orElse(item.getSets().isEmpty() ? null : item.getSets().getLast());
        if (source == null) {
            return null;
        }
        return new SetSuggestion(source.getWeightKg(), source.getRepetitions(), source.getRestSeconds());
    }

    private SessionResponse toResponse(Long userId, TrainingSession session) {
        RoutineSummary routine = sessionAccess.getRoutineSummaries(userId).get(session.getRoutineId());
        List<Long> exerciseIds = session.getExercises().stream().map(SessionExercise::getExerciseId).toList();
        Map<Long, ExerciseResponse> names = sessionAccess.getExercises(userId, exerciseIds);
        List<SessionExerciseResponse> exercises = session.getExercises().stream()
                .map(item -> TrainingMapper.toExercise(item, names.get(item.getExerciseId()).name(), suggestNextSet(item, userId)))
                .toList();
        return TrainingMapper.toSession(session, routine, exercises);
    }

    private SessionExerciseResponse toResponse(Long userId, SessionExercise item) {
        String name = sessionAccess.getExercises(userId, List.of(item.getExerciseId())).get(item.getExerciseId()).name();
        return TrainingMapper.toExercise(item, name, suggestNextSet(item, userId));
    }

}
