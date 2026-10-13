package ch.bernet.trainingplatform.service.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(@NotBlank  @Size(min = 15, max = 64) String currentPassword,
                                    @NotBlank @Size(min = 15, max = 64) String newPassword) {
}
