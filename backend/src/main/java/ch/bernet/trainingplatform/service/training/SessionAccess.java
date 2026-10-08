package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.RoutineService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class SessionAccess {

    private final RoutineService routineService;
    private final ExerciseService exerciseService;

    public SessionAccess(RoutineService routineService, ExerciseService exerciseService) {
        this.routineService = routineService;
        this.exerciseService = exerciseService;
    }

    public RoutineResponse getActiveRoutine(Long userId, Long routineId) {
        // TODO: routineService.getRoutine verwenden
        return null;
    }

    public Map<Long, RoutineSummary> getRoutineSummaries(Long userId) {
        // TODO: routineService.findAllSummaries als Map nach ID
        return null;
    }

    public Map<Long, ExerciseResponse> getExercises(Long userId, Collection<Long> exerciseIds) {
        // TODO: exerciseService.findByIds verwenden
        return null;
    }

}
