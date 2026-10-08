package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.persistence.routine.Routine;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;

import java.util.Map;

public final class RoutineMapper {

    private RoutineMapper() {
    }

    public static RoutineResponse toResponse(Routine routine, Map<Long, ExerciseResponse> exercises) {
        // TODO: Routine mit Übungsnamen und Muskelgruppe aus exercises abbilden
        return null;
    }

    public static RoutineSummary toSummary(Routine routine) {
        // TODO: Routine auf RoutineSummary abbilden
        return null;
    }

}
