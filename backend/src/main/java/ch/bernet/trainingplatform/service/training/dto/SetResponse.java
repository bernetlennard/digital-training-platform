package ch.bernet.trainingplatform.service.training.dto;

import java.math.BigDecimal;

public record SetResponse(Long id, int setNumber, BigDecimal weightKg, int repetitions, Integer restSeconds) {
}
