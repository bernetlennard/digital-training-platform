package ch.bernet.trainingplatform.service.exercise.dto;

public record ExerciseRequest(String name, int muscleGroupId, String description) {
    // TODO: Validierung ergänzen (@NotBlank, @Size(max = 100), @Size(max = 1000) für description)
}
