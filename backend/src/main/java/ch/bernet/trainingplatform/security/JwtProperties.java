package ch.bernet.trainingplatform.security;

import ch.bernet.trainingplatform.common.ErrorMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(@NotBlank @Size(min = 32, message = ErrorMessages.JWT_SECRET_TOO_SHORT) String secret,
                            @DefaultValue("8h") Duration validity) {
}
