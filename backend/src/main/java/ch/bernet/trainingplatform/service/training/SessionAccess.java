package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.RoutineService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
        // routineService.getRoutine verwenden
        return routineService.getRoutine(userId, routineId);
    }

    public Map<Long, RoutineSummary> getRoutineSummaries(Long userId) {
        // routineService.findAllSummaries als Map nach ID
        List<RoutineSummary> allSummaries = routineService.findAllSummaries(userId);
        Map<Long, RoutineSummary> map = new HashMap<>();
        allSummaries.forEach(summary -> {
            map.put(summary.id(), summary);
        });
        return map;
    }

    public Map<Long, ExerciseResponse> getExercises(Long userId, Collection<Long> exerciseIds) {
        // exerciseService.findByIds verwenden
        return exerciseService.findByIds(userId, exerciseIds);
    }

}
