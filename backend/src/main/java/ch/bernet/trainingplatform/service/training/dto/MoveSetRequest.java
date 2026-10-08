package ch.bernet.trainingplatform.service.training.dto;

public record MoveSetRequest(int newSetNumber) {
    // TODO: Validierung ergänzen (@Min(1))
}
