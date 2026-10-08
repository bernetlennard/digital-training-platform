package ch.bernet.trainingplatform.service.training.dto;

public record NoteRequest(String note) {
    // TODO: Validierung ergänzen (@Size(max = 1000))
}
