package ch.bernet.trainingplatform.service.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserResponse(Long id, @NotBlank @Email @Size(max = 254) String email,
                           @Size(max = 100) String firstName,
                           @Size(max = 100) String lastName) {
}
