package ch.bernet.trainingplatform.service.exercise;

import ch.bernet.trainingplatform.persistence.exercise.Exercise;
import ch.bernet.trainingplatform.persistence.exercise.MuscleGroup;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.exercise.dto.MuscleGroupResponse;

public final class ExerciseMapper {

    private ExerciseMapper() {
    }

    public static ExerciseResponse toResponse(Exercise exercise) {
        // Exercise auf ExerciseResponse abbilden
        return new ExerciseResponse(exercise.getId(), exercise.getName(), exercise.getMuscleGroup().getId(), exercise.getMuscleGroup().getLabel(), exercise.getDescription());
    }

    public static MuscleGroupResponse toResponse(MuscleGroup group) {
        // MuscleGroup auf MuscleGroupResponse abbilden
        return new MuscleGroupResponse(group.getId(), group.getLabel());
    }

}
