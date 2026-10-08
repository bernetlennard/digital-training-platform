package ch.bernet.trainingplatform.service.exercise.dto;

public record ExerciseResponse(Long id, String name, int muscleGroupId, String muscleGroupName, String description) {
}
