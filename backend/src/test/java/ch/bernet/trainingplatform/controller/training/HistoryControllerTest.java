package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.security.CookieBearerTokenResolver;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.security.SecurityConfig;
import ch.bernet.trainingplatform.security.SecurityErrorHandler;
import ch.bernet.trainingplatform.service.training.HistoryService;
import ch.bernet.trainingplatform.service.training.dto.HistoryRoutineResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionSummaryResponse;
import ch.bernet.trainingplatform.service.user.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;

import static ch.bernet.trainingplatform.TestData.*;
import static ch.bernet.trainingplatform.security.JwtCookieService.COOKIE_NAME;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private HistoryService historyService;

    @Test
    @DisplayName("UT-02 Verlauf ohne Token ergibt 401")
    void getHistoryRoutines_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/history/routines"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(historyService);
    }

    @Test
    @DisplayName("UT-15 Routinen im Verlauf werden mit Löschkennzeichen geliefert")
    void getHistoryRoutines_returns200() throws Exception {
        // Arrange
        when(historyService.getHistoryRoutines(USER_A)).thenReturn(List.of(
                new HistoryRoutineResponse(ROUTINE_ID, "Push", false),
                new HistoryRoutineResponse(21L, "Beine", true)));

        // Act & Assert
        mockMvc.perform(get("/api/history/routines").cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routineId").value(ROUTINE_ID))
                .andExpect(jsonPath("$[0].deleted").value(false))
                .andExpect(jsonPath("$[1].name").value("Beine"))
                .andExpect(jsonPath("$[1].deleted").value(true));
    }

    @Test
    @DisplayName("UT-15 Einheiten einer Routine auflisten ergibt 200")
    void getRoutineHistory_returns200() throws Exception {
        // Arrange
        when(historyService.getRoutineHistory(USER_A, ROUTINE_ID)).thenReturn(List.of(
                new SessionSummaryResponse(SESSION_ID, NOW, NOW.plus(Duration.ofHours(1)))));

        // Act & Assert
        mockMvc.perform(get("/api/history/routines/{routineId}/sessions", ROUTINE_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SESSION_ID))
                .andExpect(jsonPath("$[0].startedAt").value("2026-10-15T08:00:00Z"))
                .andExpect(jsonPath("$[0].finishedAt").value("2026-10-15T09:00:00Z"));
    }

    @Test
    @DisplayName("UT-15 Abgeschlossene Einheit im Detail lesen ergibt 200")
    void getSessionDetail_returns200() throws Exception {
        // Arrange
        when(historyService.getSessionDetail(USER_A, SESSION_ID)).thenReturn(new SessionResponse(SESSION_ID, ROUTINE_ID,
                "Push", true, "abgeschlossen", NOW, NOW.plus(Duration.ofHours(1)), "Neuer Rekord", List.of()));

        // Act & Assert
        mockMvc.perform(get("/api/history/sessions/{sessionId}", SESSION_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("abgeschlossen"))
                .andExpect(jsonPath("$.routineDeleted").value(true))
                .andExpect(jsonPath("$.note").value("Neuer Rekord"));
    }

    @Test
    @DisplayName("UT-15 Nicht abgeschlossene oder fremde Einheit ergibt 404")
    void getSessionDetail_notCompleted_returns404() throws Exception {
        // Arrange
        when(historyService.getSessionDetail(USER_A, SESSION_ID)).thenThrow(new NotFoundException("Die Einheit wurde nicht gefunden."));

        // Act & Assert
        mockMvc.perform(get("/api/history/sessions/{sessionId}", SESSION_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("UT-15 Einheit aus dem Verlauf löschen ergibt 204")
    void deleteSession_returns204() throws Exception {
        mockMvc.perform(delete("/api/history/sessions/{sessionId}", SESSION_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(historyService).deleteSession(USER_A, SESSION_ID);
    }

    @Test
    @DisplayName("UT-15 Nicht abgeschlossene Einheit löschen ergibt 404")
    void deleteSession_notCompleted_returns404() throws Exception {
        // Arrange
        doThrow(new NotFoundException("Die Einheit wurde nicht gefunden.")).when(historyService).deleteSession(USER_A, SESSION_ID);

        // Act & Assert
        mockMvc.perform(delete("/api/history/sessions/{sessionId}", SESSION_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private Cookie tokenCookie(Long userId) {
        when(userService.exists(userId)).thenReturn(true);
        return new Cookie(COOKIE_NAME, jwtCookieService.createAuthCookie(userId).getValue());
    }

}
