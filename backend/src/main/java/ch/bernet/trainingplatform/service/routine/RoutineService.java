package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.common.BusinessRuleException;
import ch.bernet.trainingplatform.common.ConflictException;
import ch.bernet.trainingplatform.common.ErrorMessages;
import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.persistence.routine.Routine;
import ch.bernet.trainingplatform.persistence.routine.RoutineExercise;
import ch.bernet.trainingplatform.persistence.routine.RoutineRepository;
import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoutineService {

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
        List<Routine> routineList = routineRepository.findActiveByUserId(userId);
        List<RoutineResponse> responseList = new ArrayList<>();
        routineList.forEach(routine -> {
            Map<Long, ExerciseResponse> exercisesByIds = getExercisesByIds(userId, routine);
            responseList.add(RoutineMapper.toResponse(routine, exercisesByIds));
        });
        return responseList;
    }

    @Transactional(readOnly = true)
    public RoutineResponse getRoutine(Long userId, Long routineId) {
        // findActiveRoutine, Übungen auflösen, mappen
        Routine routine = findActiveRoutine(userId, routineId);
        return RoutineMapper.toResponse(routine, getExercisesByIds(userId, routine));
    }

    @Transactional
    public RoutineResponse createRoutine(Long userId, RoutineRequest request) {
        // assertNameUnique, assertValidExercises, Routine anlegen, replaceExercises, speichern
        assertNameUnique(userId, request.name(), null);
        assertValidExercises(userId, request.exerciseIds());

        Routine routine = new Routine(userId, request.name());
        routine.replaceExercises(request.exerciseIds());
        routineRepository.save(routine);
        return RoutineMapper.toResponse(routine, getExercisesByIds(userId, routine));
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
        return RoutineMapper.toResponse(routine, getExercisesByIds(userId, routine));
    }

    @Transactional
    public void deleteRoutine(Long userId, Long routineId) {
        // Soft Delete, danach routineDeletionListener.onRoutineDeleted
        Routine routine = findActiveRoutine(userId, routineId);
        routine.setDeleted(true);
        routineRepository.save(routine);
        routineDeletionListener.onRoutineDeleted(userId, routineId);
    }

    @Transactional(readOnly = true)
    public List<RoutineSummary> findAllSummaries(Long userId) {
        // alle Routinen (auch gelöschte) als RoutineSummary
        List<Routine> allByUserId = routineRepository.findAllByUserId(userId);
        return allByUserId.stream().map(RoutineMapper::toSummary).toList();
    }

    private Routine findActiveRoutine(Long userId, Long routineId) {
        // aktive Routine laden, sonst NotFoundException
        return routineRepository.findActiveByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.ROUTINE_NOT_FOUND));
    }

    private Map<Long, ExerciseResponse> getExercisesByIds(Long userId, Routine routine) {
        return exerciseService.findByIds(userId, routine.getExercises().stream().map(RoutineExercise::getExerciseId).toList());
    }

    private void assertNameUnique(Long userId, String name, Long routineId) {
        // gleicher Name bei anderer aktiver Routine -> ConflictException (routineId darf null sein)
        routineRepository.findActiveByUserIdAndName(userId, name).ifPresent(e -> {
            if (e.getId().equals(routineId)) {
                return;
            }
            throw new ConflictException(ErrorMessages.NAME_ALREADY_IN_USE);
        });

    }

    private void assertValidExercises(Long userId, List<Long> exerciseIds) {
        // keine Duplikate, exerciseService.assertActiveExercises
        Set<Long> exerciseIdsSet = new HashSet<>(exerciseIds);
        if (exerciseIds.size() != exerciseIdsSet.size()) {
            throw new BusinessRuleException(ErrorMessages.DUPLICATE_EXERCISE_IDS);
        }
        exerciseService.assertActiveExercises(userId, exerciseIds);
    }

}
