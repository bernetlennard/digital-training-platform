package ch.bernet.trainingplatform.service.user.dto;

public record LoginRequest(String email, String password) {
    // TODO: Validierung ergänzen (@NotBlank)
}
