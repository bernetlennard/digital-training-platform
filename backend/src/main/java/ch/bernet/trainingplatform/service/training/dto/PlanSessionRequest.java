package ch.bernet.trainingplatform.service.training.dto;

import java.time.LocalDate;

public record PlanSessionRequest(Long routineId, LocalDate plannedDate) {
    // TODO: Validierung ergänzen (@NotNull)
}
