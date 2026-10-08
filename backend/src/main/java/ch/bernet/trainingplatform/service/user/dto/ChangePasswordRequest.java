package ch.bernet.trainingplatform.service.user.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {
    // TODO: Validierung ergänzen (@NotBlank, Passwortregeln)
}
