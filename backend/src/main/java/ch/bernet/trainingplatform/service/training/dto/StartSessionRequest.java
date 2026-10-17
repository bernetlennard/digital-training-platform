package ch.bernet.trainingplatform.service.training.dto;

import jakarta.validation.constraints.NotNull;

public record StartSessionRequest(@NotNull Long routineId) {
}
