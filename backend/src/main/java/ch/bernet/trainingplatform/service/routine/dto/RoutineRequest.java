package ch.bernet.trainingplatform.service.routine.dto;

import java.util.List;

public record RoutineRequest(String name, List<Long> exerciseIds) {
}
