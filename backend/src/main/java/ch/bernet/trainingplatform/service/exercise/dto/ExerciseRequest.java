package ch.bernet.trainingplatform.service.exercise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExerciseRequest(@NotBlank @Size(max = 100) String name,
                              @Min(1) @Max(8) int muscleGroupId,
                              @Size(max = 1000) String description) {
}
