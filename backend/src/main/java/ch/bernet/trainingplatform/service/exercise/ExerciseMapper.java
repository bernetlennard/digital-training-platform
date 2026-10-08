package ch.bernet.trainingplatform.service.exercise;

import ch.bernet.trainingplatform.persistence.exercise.Exercise;
import ch.bernet.trainingplatform.persistence.exercise.MuscleGroup;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.exercise.dto.MuscleGroupResponse;

public final class ExerciseMapper {

    private ExerciseMapper() {
    }

    public static ExerciseResponse toResponse(Exercise exercise) {
        // TODO: Exercise auf ExerciseResponse abbilden
        return null;
    }

    public static MuscleGroupResponse toResponse(MuscleGroup group) {
        // TODO: MuscleGroup auf MuscleGroupResponse abbilden
        return null;
    }

}
