package ch.bernet.trainingplatform.security;

import ch.bernet.trainingplatform.controller.user.AuthController;
import ch.bernet.trainingplatform.controller.user.UserController;
import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.LoginRequest;
import ch.bernet.trainingplatform.service.user.dto.RegisterRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.time.Duration;

import static ch.bernet.trainingplatform.TestData.*;
import static ch.bernet.trainingplatform.security.JwtCookieService.COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, UserController.class})
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class SecurityConfigTest {

    private static final String PASSWORD = "ein-langes-passwort";
    private static final String CREDENTIALS = """
            {"email": "a@test.ch", "password": "%s"}
            """.formatted(PASSWORD);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("UT-02 Geschützter Endpunkt ohne Token ergibt 401")
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpectAll(unauthorizedErrorResponse());

        verify(userService, never()).getProfile(any());
    }

    @Test
    @DisplayName("UT-02 Abgelaufenes Token ergibt 401")
    void protectedEndpoint_expiredToken_returns401() throws Exception {
        // Arrange: Token vor 9 Stunden ausgestellt, also seit 1 Stunde abgelaufen
        when(userService.exists(USER_A)).thenReturn(true);
        JwtCookieService pastIssuer = new JwtCookieService(jwtEncoder, jwtProperties,
                Clock.offset(Clock.systemUTC(), Duration.ofHours(-9)));

        // Act & Assert
        mockMvc.perform(get("/api/users/me").cookie(tokenCookie(pastIssuer)))
                .andExpectAll(unauthorizedErrorResponse());

        verify(userService, never()).getProfile(any());
    }

    @Test
    @DisplayName("UT-02 Falsch signiertes Token ergibt 401")
    void protectedEndpoint_wrongSignature_returns401() throws Exception {
        // Arrange: Token mit einem fremden Schlüssel signiert
        when(userService.exists(USER_A)).thenReturn(true);
        SecretKeySpec foreignKey = new SecretKeySpec("fedcba9876543210fedcba9876543210".getBytes(), "HmacSHA256");
        JwtCookieService foreignIssuer = new JwtCookieService(new NimbusJwtEncoder(new ImmutableSecret<>(foreignKey)),
                jwtProperties, Clock.systemUTC());

        // Act & Assert
        mockMvc.perform(get("/api/users/me").cookie(tokenCookie(foreignIssuer)))
                .andExpectAll(unauthorizedErrorResponse());

        verify(userService, never()).getProfile(any());
    }

    @Test
    @DisplayName("UT-02 Token im Authorization-Header wird nicht akzeptiert")
    void protectedEndpoint_tokenInHeader_returns401() throws Exception {
        // Arrange: gültiges Token, aber nicht im Cookie
        when(userService.exists(USER_A)).thenReturn(true);
        String token = jwtCookieService.createAuthCookie(USER_A).getValue();

        // Act & Assert
        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpectAll(unauthorizedErrorResponse());

        verify(userService, never()).getProfile(any());
    }

    @Test
    @DisplayName("UT-02 Registrierung und Login sind ohne Token erreichbar")
    void publicEndpoints_withoutToken_areReachable() throws Exception {
        // Arrange
        UserResponse userResponse = new UserResponse(USER_A, "a@test.ch", null, null);
        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);
        when(userService.authenticate(any(LoginRequest.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(CREDENTIALS))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("UT-05 Token eines gelöschten Kontos ergibt 401")
    void protectedEndpoint_tokenOfDeletedUser_returns401() throws Exception {
        // Arrange: Token ist gültig, das Konto existiert aber nicht mehr (#NFA-1.5)
        Cookie token = tokenCookie(jwtCookieService);
        when(userService.exists(USER_A)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/users/me").cookie(token))
                .andExpectAll(unauthorizedErrorResponse());

        verify(userService, never()).getProfile(any());
    }

    @Test
    @DisplayName("UT-03 Passwörter werden mit Argon2id gehasht")
    void passwordEncoder_usesArgon2id() {
        // Act
        String hash = passwordEncoder.encode(PASSWORD);

        // Assert
        assertThat(hash).startsWith("$argon2id$").doesNotContain(PASSWORD);
        assertThat(passwordEncoder.matches(PASSWORD, hash)).isTrue();
        assertThat(passwordEncoder.matches("ein-falsches-passwort", hash)).isFalse();
    }

    private static Cookie tokenCookie(JwtCookieService issuer) {
        return new Cookie(COOKIE_NAME, issuer.createAuthCookie(USER_A).getValue());
    }

    // 401 im einheitlichen Fehlerformat, geschrieben vom SecurityErrorHandler
    private static ResultMatcher[] unauthorizedErrorResponse() {
        return new ResultMatcher[]{
                status().isUnauthorized(),
                jsonPath("$.status").value(401),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.details").isEmpty()};
    }

}
