package ch.bernet.trainingplatform.service.user.dto;

public record RegisterRequest(String email, String password) {
    // TODO: Validierung ergänzen (@NotBlank, @Email, @Size(max = 254), Passwortregeln)
}
