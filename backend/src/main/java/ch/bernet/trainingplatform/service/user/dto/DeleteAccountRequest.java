package ch.bernet.trainingplatform.service.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeleteAccountRequest(@NotBlank @Size(min = 15, max = 64) String password) {
}
