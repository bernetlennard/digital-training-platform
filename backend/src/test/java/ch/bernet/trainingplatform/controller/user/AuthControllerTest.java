package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.common.AccountLockedException;
import ch.bernet.trainingplatform.common.AuthenticationFailedException;
import ch.bernet.trainingplatform.common.ConflictException;
import ch.bernet.trainingplatform.security.CookieBearerTokenResolver;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.security.SecurityConfig;
import ch.bernet.trainingplatform.security.SecurityErrorHandler;
import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.LoginRequest;
import ch.bernet.trainingplatform.service.user.dto.RegisterRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static ch.bernet.trainingplatform.TestData.*;
import static ch.bernet.trainingplatform.security.JwtCookieService.COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class AuthControllerTest {

    private static final String EMAIL = "a@test.ch";
    private static final String PASSWORD = "ein-langes-passwort";
    private static final UserResponse USER_RESPONSE = new UserResponse(USER_A, EMAIL, null, null);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("UT-03 Registrieren ergibt 201 mit Token-Cookie")
    void register_valid_returns201WithCookie() throws Exception {
        // Arrange
        when(userService.register(any(RegisterRequest.class))).thenReturn(USER_RESPONSE);

        // Act & Assert
        mockMvc.perform(postJson("/api/auth/register", credentials(EMAIL, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_A))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpectAll(authCookie());

        verify(userService).register(new RegisterRequest(EMAIL, PASSWORD));
    }

    @Test
    @DisplayName("UT-01 Ungültige Eingaben ergeben 400 mit Feldfehlern")
    void register_invalidInput_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(postJson("/api/auth/register", credentials("keine-email", "zu-kurz")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.details", hasItem(startsWith("Validation error: email:"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("Validation error: password:"))))
                .andExpect(content().string(not(containsString("zu-kurz"))))
                .andExpect(content().string(not(containsString("keine-email"))));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("UT-03 Vergebene E-Mail ergibt 409")
    void register_existingEmail_returns409() throws Exception {
        // Arrange
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("Die E-Mail-Adresse ist bereits registriert."));

        // Act & Assert
        mockMvc.perform(postJson("/api/auth/register", credentials(EMAIL, PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Die E-Mail-Adresse ist bereits registriert."))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(cookie().doesNotExist(COOKIE_NAME));
    }

    @Test
    @DisplayName("UT-03 Anmelden ergibt 200 mit Token-Cookie")
    void login_valid_returns200WithCookie() throws Exception {
        // Arrange
        when(userService.authenticate(any(LoginRequest.class))).thenReturn(USER_RESPONSE);

        // Act & Assert
        mockMvc.perform(postJson("/api/auth/login", credentials(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_A))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpectAll(authCookie());

        verify(userService).authenticate(new LoginRequest(EMAIL, PASSWORD));
    }

    @Test
    @DisplayName("UT-04 Falsches Passwort und unbekannte E-Mail ergeben dieselbe 401-Antwort")
    void login_wrongPasswordOrUnknownEmail_returnsIdentical401() throws Exception {
        // Arrange
        when(userService.authenticate(any(LoginRequest.class)))
                .thenThrow(new AuthenticationFailedException("E-Mail-Adresse oder Passwort ist falsch."));

        // Act
        MvcResult wrongPassword = mockMvc.perform(postJson("/api/auth/login", credentials(EMAIL, "ein-falsches-passwort")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("E-Mail-Adresse oder Passwort ist falsch."))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andReturn();

        MvcResult unknownEmail = mockMvc.perform(postJson("/api/auth/login", credentials("unbekannt@test.ch", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andReturn();

        // Assert
        assertThat(unknownEmail.getResponse().getContentAsString())
                .isEqualTo(wrongPassword.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("UT-04 Gesperrte E-Mail ergibt 429")
    void login_locked_returns429() throws Exception {
        // Arrange
        when(userService.authenticate(any(LoginRequest.class)))
                .thenThrow(new AccountLockedException("Zu viele Fehlversuche. Bitte in 15 Minuten erneut versuchen."));

        // Act & Assert
        mockMvc.perform(postJson("/api/auth/login", credentials(EMAIL, PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").value("Zu viele Fehlversuche. Bitte in 15 Minuten erneut versuchen."))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(cookie().doesNotExist(COOKIE_NAME));
    }

    @Test
    @DisplayName("UT-05 Abmelden ergibt 204 und löscht das Cookie")
    void logout_returns204AndDeletesCookie() throws Exception {
        // Arrange
        when(userService.exists(USER_A)).thenReturn(true);
        Cookie token = new Cookie(COOKIE_NAME, jwtCookieService.createAuthCookie(USER_A).getValue());

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout").cookie(token))
                .andExpect(status().isNoContent())
                .andExpect(cookie().value(COOKIE_NAME, ""))
                .andExpect(cookie().maxAge(COOKIE_NAME, 0))
                .andExpect(cookie().path(COOKIE_NAME, "/"))
                .andExpect(cookie().httpOnly(COOKIE_NAME, true))
                .andExpect(cookie().secure(COOKIE_NAME, true))
                .andExpect(cookie().sameSite(COOKIE_NAME, "Strict"));
    }

    private static MockHttpServletRequestBuilder postJson(String path, String json) {
        return post(path).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private static String credentials(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }

    // Cookie gemäss #NFA-1.2: HttpOnly, Secure, SameSite=Strict, 8 Stunden gültig
    private static ResultMatcher[] authCookie() {
        return new ResultMatcher[]{
                cookie().value(COOKIE_NAME, not(emptyOrNullString())),
                cookie().path(COOKIE_NAME, "/"),
                cookie().httpOnly(COOKIE_NAME, true),
                cookie().secure(COOKIE_NAME, true),
                cookie().sameSite(COOKIE_NAME, "Strict"),
                cookie().maxAge(COOKIE_NAME, 8 * 60 * 60)};
    }

}
