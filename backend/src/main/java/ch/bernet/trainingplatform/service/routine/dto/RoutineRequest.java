package ch.bernet.trainingplatform.service.routine.dto;

import java.util.List;

public record RoutineRequest(String name, List<Long> exerciseIds) {
    // TODO: Validierung ergänzen (@NotBlank, @Size(max = 100), @NotEmpty für exerciseIds)
}
