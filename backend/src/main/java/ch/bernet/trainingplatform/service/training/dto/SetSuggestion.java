package ch.bernet.trainingplatform.service.training.dto;

import java.math.BigDecimal;

public record SetSuggestion(BigDecimal weightKg, int repetitions, Integer restSeconds) {
}
