package ch.bernet.trainingplatform.service.training.dto;

import java.time.Instant;

public record SessionSummaryResponse(Long id, Instant startedAt, Instant finishedAt) {
}
