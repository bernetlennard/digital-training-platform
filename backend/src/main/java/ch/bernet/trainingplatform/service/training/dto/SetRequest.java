package ch.bernet.trainingplatform.service.training.dto;

import java.math.BigDecimal;

public record SetRequest(BigDecimal weightKg, int repetitions, Integer restSeconds) {
    // TODO: Validierung ergänzen (@NotNull, 0-1000 kg, 1-100 Wdh., 0-3600 s Pause)
}
