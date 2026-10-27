package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.common.exception.BusinessRuleException;
import ch.bernet.trainingplatform.common.exception.ConflictException;
import ch.bernet.trainingplatform.common.exception.ErrorMessages;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import ch.bernet.trainingplatform.persistence.routine.Routine;
import ch.bernet.trainingplatform.persistence.routine.RoutineExercise;
import ch.bernet.trainingplatform.persistence.routine.RoutineRepository;
import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RoutineService {

    private static final Logger log = LoggerFactory.getLogger(RoutineService.class);

    private final RoutineRepository routineRepository;
    private final ExerciseService exerciseService;
    private final RoutineDeletionListener routineDeletionListener;

    public RoutineService(RoutineRepository routineRepository,
                          ExerciseService exerciseService,
                          RoutineDeletionListener routineDeletionListener) {
        this.routineRepository = routineRepository;
        this.exerciseService = exerciseService;
        this.routineDeletionListener = routineDeletionListener;
    }

    @Transactional(readOnly = true)
    public List<RoutineResponse> getRoutines(Long userId) {
        // aktive Routinen laden, Übungen über exerciseService.findByIds auflösen, mappen
        return routineRepository.findActiveByUserId(userId).stream()
                .map(routine -> toResponse(userId, routine))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoutineResponse getRoutine(Long userId, Long routineId) {
        // findActiveRoutine, Übungen auflösen, mappen
        return toResponse(userId, findActiveRoutine(userId, routineId));
    }

    @Transactional
    public RoutineResponse createRoutine(Long userId, RoutineRequest request) {
        // assertNameUnique, assertValidExercises, Routine anlegen, replaceExercises, speichern
        assertNameUnique(userId, request.name(), null);
        assertValidExercises(userId, request.exerciseIds());

        Routine routine = new Routine(userId, request.name());
        routine.replaceExercises(request.exerciseIds());
        routineRepository.save(routine);
        log.info("Routine {} created, user {}", routine.getId(), userId);
        return toResponse(userId, routine);
    }

    @Transactional
    public RoutineResponse updateRoutine(Long userId, Long routineId, RoutineRequest request) {
        // findActiveRoutine, assertNameUnique (eigene ID ausnehmen), assertValidExercises, Name und Übungen ersetzen
        Routine routine = findActiveRoutine(userId, routineId);
        assertNameUnique(userId, request.name(), routineId);
        assertValidExercises(userId, request.exerciseIds());
        routine.setName(request.name());
        routine.replaceExercises(request.exerciseIds());
        routineRepository.save(routine);
        log.info("Routine {} updated, user {}", routineId, userId);
        return toResponse(userId, routine);
    }

    @Transactional
    public void deleteRoutine(Long userId, Long routineId) {
        // Soft Delete, danach routineDeletionListener.onRoutineDeleted
        Routine routine = findActiveRoutine(userId, routineId);
        routine.setDeleted(true);
        routineRepository.save(routine);
        routineDeletionListener.onRoutineDeleted(userId, routineId);
        log.info("Routine {} deleted, user {}", routineId, userId);
    }

    @Transactional(readOnly = true)
    public List<RoutineSummary> findAllSummaries(Long userId) {
        // alle Routinen (auch gelöschte) als RoutineSummary
        return routineRepository.findAllByUserId(userId).stream()
                .map(RoutineMapper::toSummary)
                .toList();
    }

    private Routine findActiveRoutine(Long userId, Long routineId) {
        // aktive Routine laden, sonst NotFoundException
        return routineRepository.findActiveByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ROUTINE_NOT_FOUND));
    }

    private RoutineResponse toResponse(Long userId, Routine routine) {
        List<Long> exerciseIds = routine.getExercises().stream().map(RoutineExercise::getExerciseId).toList();
        Map<Long, ExerciseResponse> exercises = exerciseService.findByIds(userId, exerciseIds);
        return RoutineMapper.toResponse(routine, exercises);
    }

    private void assertNameUnique(Long userId, String name, Long routineId) {
        // gleicher Name bei anderer aktiver Routine -> ConflictException (routineId darf null sein)
        routineRepository.findActiveByUserIdAndName(userId, name)
                .filter(existing -> !existing.getId().equals(routineId))
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorMessages.NAME_ALREADY_IN_USE);
                });
    }

    private void assertValidExercises(Long userId, List<Long> exerciseIds) {
        // keine Duplikate, exerciseService.assertActiveExercises
        Set<Long> uniqueIds = new HashSet<>(exerciseIds);
        if (exerciseIds.size() != uniqueIds.size()) {
            throw new BusinessRuleException(ErrorMessages.DUPLICATE_EXERCISE_IDS);
        }
        exerciseService.assertActiveExercises(userId, exerciseIds);
    }

}
