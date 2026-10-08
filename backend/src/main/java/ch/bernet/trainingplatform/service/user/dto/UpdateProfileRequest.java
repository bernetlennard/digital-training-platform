package ch.bernet.trainingplatform.service.user.dto;

public record UpdateProfileRequest(String firstName, String lastName) {
    // TODO: Validierung ergänzen (@Size(max = 100))
}
