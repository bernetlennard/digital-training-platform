package ch.bernet.trainingplatform.service.user.dto;

public record DeleteAccountRequest(String password) {
    // TODO: Validierung ergänzen (@NotBlank)
}
