package ch.bernet.trainingplatform.service.user.dto;

public record ChangeEmailRequest(String newEmail, String password) {
    // TODO: Validierung ergänzen (@NotBlank, @Email, @Size(max = 254))
}
