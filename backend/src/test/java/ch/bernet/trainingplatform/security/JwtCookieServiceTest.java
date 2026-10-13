package ch.bernet.trainingplatform.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

class JwtCookieServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private JwtCookieService jwtCookieService;
    private NimbusJwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
        JwtProperties jwtProperties = new JwtProperties(SECRET, Duration.ofHours(8));
        jwtCookieService = new JwtCookieService(new NimbusJwtEncoder(new ImmutableSecret<>(key)), jwtProperties, CLOCK);

        // Ablauf gegen die feste Uhr prüfen, nicht gegen die Systemzeit
        JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
        timestampValidator.setClock(CLOCK);
        jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtDecoder.setJwtValidator(timestampValidator);
    }

    @Test
    @DisplayName("UT-03 Cookie ist HttpOnly, Secure, SameSite=Strict und 8 Stunden gültig")
    void createAuthCookie_hasSecurityAttributes() {
        // Act
        ResponseCookie cookie = jwtCookieService.createAuthCookie(USER_A);

        // Assert
        assertThat(cookie.getName()).isEqualTo(JwtCookieService.COOKIE_NAME);
        assertThat(cookie.getValue()).isNotBlank();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofHours(8));
    }

    @Test
    @DisplayName("UT-03 Token enthält nur die Benutzer-ID und läuft nach 8 Stunden ab")
    void createAuthCookie_tokenContainsUserIdAndExpiry() {
        // Act
        Jwt jwt = jwtDecoder.decode(jwtCookieService.createAuthCookie(USER_A).getValue());

        // Assert
        assertThat(jwt.getSubject()).isEqualTo(USER_A.toString());
        assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(8)));
        // Keine weiteren Personendaten wie die E-Mail-Adresse im Token
        assertThat(jwt.getClaims()).containsOnlyKeys("sub", "iat", "exp");
    }

    @Test
    @DisplayName("UT-05 Logout-Cookie läuft sofort ab")
    void createLogoutCookie_expiresImmediately() {
        // Act
        ResponseCookie cookie = jwtCookieService.createLogoutCookie();

        // Assert
        assertThat(cookie.getName()).isEqualTo(JwtCookieService.COOKIE_NAME);
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
    }

}
