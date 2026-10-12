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

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("UT-02 Die Benutzer-ID kommt aus dem Token und nicht aus dem Request")
    void updateProfile_usesUserIdFromToken() throws Exception {
    }

    @Test
    @DisplayName("UT-05 Passwort ändern ergibt 204 mit neuem Token-Cookie")
    void changePassword_valid_returns204WithNewCookie() throws Exception {
    }

    @Test
    @DisplayName("UT-05 Konto löschen ergibt 204 und löscht das Cookie")
    void deleteAccount_valid_returns204AndDeletesCookie() throws Exception {
    }

}
