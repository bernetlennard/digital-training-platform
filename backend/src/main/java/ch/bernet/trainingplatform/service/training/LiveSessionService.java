package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.common.exception.ConflictException;
import ch.bernet.trainingplatform.common.exception.ErrorMessages;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import ch.bernet.trainingplatform.persistence.training.ExerciseSetRepository;
import ch.bernet.trainingplatform.persistence.training.SessionExercise;
import ch.bernet.trainingplatform.persistence.training.SessionStatus;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        // schon eine aktive Einheit -> ConflictException, geplante Einheit laden, start mit Übungen der Routine
        assertNoActiveSession(userId);
        TrainingSession session = trainingSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.SESSION_NOT_FOUND));
        List<Long> exerciseIds = getRoutineExerciseIds(userId, session.getRoutineId());
        session.start(clock.instant(), exerciseIds);
        trainingSessionRepository.flush();
        return toResponse(userId, session);
    }

    @Transactional
    public SessionResponse startSpontaneousSession(Long userId, StartSessionRequest request) {
        // schon eine aktive Einheit -> ConflictException, Einheit ohne Datum anlegen und starten
        assertNoActiveSession(userId);
        TrainingSession session = new TrainingSession(userId, request.routineId(), null);
        List<Long> exerciseIds = getRoutineExerciseIds(userId, request.routineId());
        session.start(clock.instant(), exerciseIds);
        trainingSessionRepository.saveAndFlush(session);
        return toResponse(userId, session);
    }

    @Transactional(readOnly = true)
    public SessionResponse getActiveSession(Long userId) {
        // aktive Einheit oder null zurückgeben, inkl. offener Satzvorschläge pro Übung
        return findActiveSession(userId).map(session -> toResponse(userId, session)).orElse(null);
    }

    @Transactional
    public void updateNote(Long userId, NoteRequest request) {
        // Notiz der aktiven Einheit setzen
        getActiveOrThrow(userId).setNote(request.note());
    }

    @Transactional
    public SessionExerciseResponse addSet(Long userId, Long sessionExerciseId, SetRequest request) {
        // getActiveOrThrow, getExercise, addSet; flush, damit der neue Satz eine ID hat
        TrainingSession session = getActiveOrThrow(userId);
        SessionExercise item = session.getExercise(sessionExerciseId);
        item.addSet(request.weightKg(), request.repetitions(), request.restSeconds());
        trainingSessionRepository.flush();
        return toResponse(userId, session, item);
    }

    @Transactional
    public SessionExerciseResponse updateSet(Long userId, Long setId, SetRequest request) {
        // getActiveOrThrow, getExerciseOfSet, updateSet
        TrainingSession session = getActiveOrThrow(userId);
        SessionExercise item = session.getExerciseOfSet(setId);
        item.updateSet(setId, request.weightKg(), request.repetitions(), request.restSeconds());
        return toResponse(userId, session, item);
    }

    @Transactional
    public SessionExerciseResponse moveSet(Long userId, Long setId, MoveSetRequest request) {
        // getActiveOrThrow, getExerciseOfSet, moveSet
        TrainingSession session = getActiveOrThrow(userId);
        SessionExercise item = session.getExerciseOfSet(setId);
        item.moveSet(setId, request.newSetNumber());
        return toResponse(userId, session, item);
    }

    @Transactional
    public SessionExerciseResponse deleteSet(Long userId, Long setId) {
        // getActiveOrThrow, getExerciseOfSet, removeSet
        TrainingSession session = getActiveOrThrow(userId);
        SessionExercise item = session.getExerciseOfSet(setId);
        item.removeSet(setId);
        return toResponse(userId, session, item);
    }

    @Transactional
    public void finishSession(Long userId) {
        // getActiveOrThrow, finish mit clock.instant()
        getActiveOrThrow(userId).finish(clock.instant());
    }

    @Transactional
    public void cancelSession(Long userId) {
        // geplante Einheit -> revertToPlanned, spontane Einheit -> hart löschen.
        // Wurde die Routine während des Trainings gelöscht, ebenfalls löschen: Ihre geplanten Einheiten
        // sind schon weg, und eine Einheit mit gelöschter Routine lässt sich nicht mehr starten.
        TrainingSession session = getActiveOrThrow(userId);
        if (session.isSpontaneous() || isRoutineDeleted(userId, session.getRoutineId())) {
            trainingSessionRepository.delete(session);
        } else {
            session.revertToPlanned();
        }
    }

    private Optional<TrainingSession> findActiveSession(Long userId) {
        return trainingSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE).stream().findFirst();
    }

    private TrainingSession getActiveOrThrow(Long userId) {
        // aktive Einheit laden, sonst NotFoundException
        return findActiveSession(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.NO_ACTIVE_SESSION));
    }

    private void assertNoActiveSession(Long userId) {
        if (findActiveSession(userId).isPresent()) {
            throw new ConflictException(ErrorMessages.SESSION_ALREADY_ACTIVE);
        }
    }

    private List<Long> getRoutineExerciseIds(Long userId, Long routineId) {
        return sessionAccess.getActiveRoutine(userId, routineId).exercises().stream()
                .map(RoutineExerciseResponse::exerciseId)
                .toList();
    }

    private boolean isRoutineDeleted(Long userId, Long routineId) {
        return sessionAccess.getRoutineSummaries(userId).get(routineId).deleted();
    }

    private List<SetSuggestion> suggestOpenSets(Long userId, TrainingSession session, SessionExercise item) {
        // Sätze der letzten Einheit derselben Routine, die heute noch fehlen; danach bleibt alles leer
        int recordedSets = item.getSets().size();
        return exerciseSetRepository.findLastCompletedSets(userId, session.getRoutineId(), item.getExerciseId()).stream()
                .filter(set -> set.getSetNumber() > recordedSets)
                .map(set -> new SetSuggestion(set.getSetNumber(), set.getWeightKg(), set.getRepetitions(), set.getRestSeconds()))
                .toList();
    }

    private SessionResponse toResponse(Long userId, TrainingSession session) {
        RoutineSummary routine = sessionAccess.getRoutineSummaries(userId).get(session.getRoutineId());
        List<Long> exerciseIds = session.getExercises().stream().map(SessionExercise::getExerciseId).toList();
        Map<Long, ExerciseResponse> names = sessionAccess.getExercises(userId, exerciseIds);
        List<SessionExerciseResponse> exercises = session.getExercises().stream()
                .map(item -> TrainingMapper.toExercise(item, names.get(item.getExerciseId()).name(),
                        suggestOpenSets(userId, session, item)))
                .toList();
        return TrainingMapper.toSession(session, routine, exercises);
    }

    private SessionExerciseResponse toResponse(Long userId, TrainingSession session, SessionExercise item) {
        String name = sessionAccess.getExercises(userId, List.of(item.getExerciseId())).get(item.getExerciseId()).name();
        return TrainingMapper.toExercise(item, name, suggestOpenSets(userId, session, item));
    }

}
