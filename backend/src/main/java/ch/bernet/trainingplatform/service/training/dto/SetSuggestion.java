package ch.bernet.trainingplatform.service.training.dto;

import java.math.BigDecimal;

public record SetSuggestion(int setNumber, BigDecimal weightKg, int repetitions, Integer restSeconds) {
}
