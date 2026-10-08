package ch.bernet.trainingplatform.service.routine.dto;

import java.util.List;

public record RoutineResponse(Long id, String name, List<RoutineExerciseResponse> exercises) {
}
