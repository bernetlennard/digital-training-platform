package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.security.CookieBearerTokenResolver;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.security.SecurityConfig;
import ch.bernet.trainingplatform.security.SecurityErrorHandler;
import ch.bernet.trainingplatform.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("UT-03 Registrieren ergibt 201 mit Token-Cookie")
    void register_valid_returns201WithCookie() throws Exception {
    }

    @Test
    @DisplayName("UT-01 Ungültige Eingaben ergeben 400 mit Feldfehlern")
    void register_invalidInput_returns400WithFieldErrors() throws Exception {
    }

    @Test
    @DisplayName("UT-03 Vergebene E-Mail ergibt 409")
    void register_existingEmail_returns409() throws Exception {
    }

    @Test
    @DisplayName("UT-03 Anmelden ergibt 200 mit Token-Cookie")
    void login_valid_returns200WithCookie() throws Exception {
    }

    @Test
    @DisplayName("UT-04 Falsches Passwort und unbekannte E-Mail ergeben dieselbe 401-Antwort")
    void login_wrongPasswordOrUnknownEmail_returnsIdentical401() throws Exception {
    }

    @Test
    @DisplayName("UT-04 Gesperrte E-Mail ergibt 429")
    void login_locked_returns429() throws Exception {
    }

    @Test
    @DisplayName("UT-05 Abmelden ergibt 204 und löscht das Cookie")
    void logout_returns204AndDeletesCookie() throws Exception {
    }

}
