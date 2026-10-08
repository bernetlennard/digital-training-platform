package ch.bernet.trainingplatform.service.training.dto;

import java.util.List;

public record SessionExerciseResponse(
        Long id,
        Long exerciseId,
        String exerciseName,
        int position,
        List<SetResponse> sets,
        SetSuggestion nextSet
) {
}
