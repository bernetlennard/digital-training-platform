package ch.bernet.trainingplatform.service.training.dto;

import java.time.LocalDate;

public record PlannedSessionResponse(Long id, Long routineId, String routineName, LocalDate plannedDate) {
}
