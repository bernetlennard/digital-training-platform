package ch.bernet.trainingplatform.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;

import static ch.bernet.trainingplatform.TestData.*;

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
    }

    @Test
    @DisplayName("UT-03 Token enthält nur die Benutzer-ID und läuft nach 8 Stunden ab")
    void createAuthCookie_tokenContainsUserIdAndExpiry() {
    }

    @Test
    @DisplayName("UT-05 Logout-Cookie läuft sofort ab")
    void createLogoutCookie_expiresImmediately() {
    }

}
