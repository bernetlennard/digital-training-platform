package ch.bernet.trainingplatform.security;

import ch.bernet.trainingplatform.controller.user.AuthController;
import ch.bernet.trainingplatform.controller.user.UserController;
import ch.bernet.trainingplatform.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AuthController.class, UserController.class})
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("UT-02 Geschützter Endpunkt ohne Token ergibt 401")
    void protectedEndpoint_withoutToken_returns401() throws Exception {
    }

    @Test
    @DisplayName("UT-02 Abgelaufenes Token ergibt 401")
    void protectedEndpoint_expiredToken_returns401() throws Exception {
    }

    @Test
    @DisplayName("UT-02 Falsch signiertes Token ergibt 401")
    void protectedEndpoint_wrongSignature_returns401() throws Exception {
    }

    @Test
    @DisplayName("UT-02 Token im Authorization-Header wird nicht akzeptiert")
    void protectedEndpoint_tokenInHeader_returns401() throws Exception {
    }

    @Test
    @DisplayName("UT-02 Registrierung und Login sind ohne Token erreichbar")
    void publicEndpoints_withoutToken_areReachable() throws Exception {
    }

    @Test
    @DisplayName("UT-05 Token eines gelöschten Kontos ergibt 401")
    void protectedEndpoint_tokenOfDeletedUser_returns401() throws Exception {
    }

    @Test
    @DisplayName("UT-03 Passwörter werden mit Argon2id gehasht")
    void passwordEncoder_usesArgon2id() {
    }

}
