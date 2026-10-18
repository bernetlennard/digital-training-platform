package ch.bernet.trainingplatform.service.training.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SetRequest(@NotNull @DecimalMin("0") @DecimalMax("1000") @Digits(integer = 4, fraction = 2) BigDecimal weightKg,
                         @Min(1) @Max(100) int repetitions,
                         @Min(0) @Max(3600) Integer restSeconds) {
}
