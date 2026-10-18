package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.persistence.routine.Routine;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RoutineMapper {

    private RoutineMapper() {
    }

    public static RoutineResponse toResponse(Routine routine, Map<Long, ExerciseResponse> exercises) {
        // Routine mit Übungsnamen und Muskelgruppe aus exercises abbilden
        List<RoutineExerciseResponse> routineExerciseResponses = new ArrayList<>();
        routine.getExercises().forEach(routineExercise -> {
            ExerciseResponse exercise = exercises.get(routineExercise.getExerciseId());
            RoutineExerciseResponse routineExerciseResponse = new RoutineExerciseResponse(routineExercise.getPosition(), routineExercise.getExerciseId(), exercise.name(), exercise.muscleGroupName());
            routineExerciseResponses.add(routineExerciseResponse);
        });
        return new RoutineResponse(routine.getId(), routine.getName(), routineExerciseResponses);
    }

    public static RoutineSummary toSummary(Routine routine) {
        // Routine auf RoutineSummary abbilden
        return new RoutineSummary(routine.getId(), routine.getName(), routine.isDeleted());
    }

}
