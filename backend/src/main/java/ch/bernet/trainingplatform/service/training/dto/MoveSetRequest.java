package ch.bernet.trainingplatform.service.training.dto;

import jakarta.validation.constraints.Min;

public record MoveSetRequest(@Min(1) int newSetNumber) {
}
