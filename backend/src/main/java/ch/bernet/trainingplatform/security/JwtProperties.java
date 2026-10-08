package ch.bernet.trainingplatform.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

// TODO: app.jwt.secret in application.properties aus Umgebungsvariable lesen (mind. 256 Bit, L9)
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, @DefaultValue("8h") Duration validity) {
}
