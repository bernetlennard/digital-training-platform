package ch.bernet.trainingplatform.service.training.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PlanSessionRequest(@NotNull Long routineId,
                                 @NotNull LocalDate plannedDate) {
}
