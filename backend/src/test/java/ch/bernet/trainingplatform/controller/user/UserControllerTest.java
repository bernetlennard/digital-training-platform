package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.security.CookieBearerTokenResolver;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.security.SecurityConfig;
import ch.bernet.trainingplatform.security.SecurityErrorHandler;
import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.ChangePasswordRequest;
import ch.bernet.trainingplatform.service.user.dto.DeleteAccountRequest;
import ch.bernet.trainingplatform.service.user.dto.UpdateProfileRequest;
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

import static ch.bernet.trainingplatform.TestData.*;
import static ch.bernet.trainingplatform.security.JwtCookieService.COOKIE_NAME;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class UserControllerTest {

    private static final String PASSWORD = "ein-langes-passwort";
    private static final String NEW_PASSWORD = "ein-neues-langes-passwort";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("UT-02 Die Benutzer-ID kommt aus dem Token und nicht aus dem Request")
    void updateProfile_usesUserIdFromToken() throws Exception {
        // Arrange
        when(userService.updateProfile(eq(USER_A), any(UpdateProfileRequest.class)))
                .thenReturn(new UserResponse(USER_A, "a@test.ch", "Anna", "Muster"));

        // Act & Assert: Der Request versucht, die ID von Sportler B unterzuschieben
        mockMvc.perform(put("/api/users/me")
                        .param("userId", USER_B.toString())
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Anna", "lastName": "Muster"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_A))
                .andExpect(jsonPath("$.firstName").value("Anna"))
                .andExpect(jsonPath("$.lastName").value("Muster"));

        verify(userService).updateProfile(USER_A, new UpdateProfileRequest("Anna", "Muster"));
        verify(userService, never()).updateProfile(eq(USER_B), any());
    }

    @Test
    @DisplayName("UT-05 Passwort ändern ergibt 204 mit neuem Token-Cookie")
    void changePassword_valid_returns204WithNewCookie() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "%s", "newPassword": "%s"}
                                """.formatted(PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isNoContent())
                // Das Token wird erneuert und ist wieder 8 Stunden gültig (#FA-1.9)
                .andExpect(cookie().value(COOKIE_NAME, not(emptyOrNullString())))
                .andExpect(cookie().httpOnly(COOKIE_NAME, true))
                .andExpect(cookie().secure(COOKIE_NAME, true))
                .andExpect(cookie().sameSite(COOKIE_NAME, "Strict"))
                .andExpect(cookie().maxAge(COOKIE_NAME, 8 * 60 * 60));

        verify(userService).changePassword(USER_A, new ChangePasswordRequest(PASSWORD, NEW_PASSWORD));
    }

    @Test
    @DisplayName("UT-05 Konto löschen ergibt 204 und löscht das Cookie")
    void deleteAccount_valid_returns204AndDeletesCookie() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().value(COOKIE_NAME, ""))
                .andExpect(cookie().maxAge(COOKIE_NAME, 0));

        verify(userService).deleteAccount(USER_A, new DeleteAccountRequest(PASSWORD));
    }

    private Cookie tokenCookie(Long userId) {
        when(userService.exists(userId)).thenReturn(true);
        return new Cookie(COOKIE_NAME, jwtCookieService.createAuthCookie(userId).getValue());
    }

}
