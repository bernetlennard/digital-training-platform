package ch.bernet.trainingplatform.service.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeEmailRequest(@NotBlank @Email @Size(max = 254) String newEmail,
                                 @NotBlank @Size(min = 15, max = 64) String password) {
}
