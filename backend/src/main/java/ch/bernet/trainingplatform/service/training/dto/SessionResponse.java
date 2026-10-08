package ch.bernet.trainingplatform.service.training.dto;

import java.time.Instant;
import java.util.List;

public record SessionResponse(
        Long id,
        Long routineId,
        String routineName,
        boolean routineDeleted,
        String status,
        Instant startedAt,
        Instant finishedAt,
        String note,
        List<SessionExerciseResponse> exercises
) {
}
