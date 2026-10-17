package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.common.exception.ConflictException;
import ch.bernet.trainingplatform.common.exception.ErrorMessages;
import ch.bernet.trainingplatform.security.CookieBearerTokenResolver;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.security.SecurityConfig;
import ch.bernet.trainingplatform.security.SecurityErrorHandler;
import ch.bernet.trainingplatform.service.training.LiveSessionService;
import ch.bernet.trainingplatform.service.training.dto.MoveSetRequest;
import ch.bernet.trainingplatform.service.training.dto.NoteRequest;
import ch.bernet.trainingplatform.service.training.dto.SessionExerciseResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SetRequest;
import ch.bernet.trainingplatform.service.training.dto.SetResponse;
import ch.bernet.trainingplatform.service.training.dto.SetSuggestion;
import ch.bernet.trainingplatform.service.training.dto.StartSessionRequest;
import ch.bernet.trainingplatform.service.user.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static ch.bernet.trainingplatform.TestData.*;
import static ch.bernet.trainingplatform.security.JwtCookieService.COOKIE_NAME;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LiveSessionController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class LiveSessionControllerTest {

    private static final SessionExerciseResponse SQUAT_EXERCISE = new SessionExerciseResponse(101L, SQUAT, "Kniebeuge", 1,
            List.of(new SetResponse(501L, 1, new BigDecimal("100.00"), 5, 180)),
            new SetSuggestion(new BigDecimal("100.00"), 5, 180));

    private static final SessionResponse ACTIVE = new SessionResponse(SESSION_ID, ROUTINE_ID, "Push", false, "aktiv",
            NOW, null, null, List.of(SQUAT_EXERCISE));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LiveSessionService liveSessionService;

    @Test
    @DisplayName("UT-02 Aktive Einheit ohne Token ergibt 401")
    void getActiveSession_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/sessions/active"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(liveSessionService);
    }

    @Test
    @DisplayName("UT-11 Geplante Einheit starten ergibt 200, weil keine neue Ressource entsteht")
    void startPlannedSession_returns200() throws Exception {
        // Arrange
        when(liveSessionService.startPlannedSession(USER_A, SESSION_ID)).thenReturn(ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/v1/sessions/planned/{sessionId}/start", SESSION_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SESSION_ID))
                .andExpect(jsonPath("$.status").value("aktiv"));
    }

    @Test
    @DisplayName("UT-11 Spontane Einheit starten ergibt 201")
    void startSpontaneousSession_returns201() throws Exception {
        // Arrange
        when(liveSessionService.startSpontaneousSession(eq(USER_A), any(StartSessionRequest.class))).thenReturn(ACTIVE);

        // Act & Assert
        mockMvc.perform(post("/api/v1/sessions")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"routineId": %d}
                                """.formatted(ROUTINE_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("aktiv"));

        verify(liveSessionService).startSpontaneousSession(USER_A, new StartSessionRequest(ROUTINE_ID));
    }

    @Test
    @DisplayName("UT-11 Spontaner Start ohne Routine ergibt 400")
    void startSpontaneousSession_withoutRoutine_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/sessions")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem(startsWith("routineId:"))));

        verifyNoInteractions(liveSessionService);
    }

    @Test
    @DisplayName("UT-11 Zweite aktive Einheit ergibt 409")
    void startSpontaneousSession_otherSessionActive_returns409() throws Exception {
        // Arrange
        when(liveSessionService.startSpontaneousSession(eq(USER_A), any(StartSessionRequest.class)))
                .thenThrow(new ConflictException(ErrorMessages.SESSION_ALREADY_ACTIVE));

        // Act & Assert
        mockMvc.perform(post("/api/v1/sessions")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"routineId": %d}
                                """.formatted(ROUTINE_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("UT-11 Aktive Einheit lesen ergibt 200 mit Zeitpunkt in UTC")
    void getActiveSession_active_returns200() throws Exception {
        // Arrange
        when(liveSessionService.getActiveSession(USER_A)).thenReturn(ACTIVE);

        // Act & Assert
        mockMvc.perform(get("/api/v1/sessions/active").cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SESSION_ID))
                .andExpect(jsonPath("$.routineName").value("Push"))
                .andExpect(jsonPath("$.routineDeleted").value(false))
                .andExpect(jsonPath("$.status").value("aktiv"))
                .andExpect(jsonPath("$.startedAt").value("2026-10-15T08:00:00Z"))
                .andExpect(jsonPath("$.exercises", hasSize(1)))
                .andExpect(jsonPath("$.exercises[0].id").value(101))
                .andExpect(jsonPath("$.exercises[0].sets[0].setNumber").value(1))
                .andExpect(jsonPath("$.exercises[0].nextSet.repetitions").value(5));
    }

    @Test
    @DisplayName("UT-11 Ohne aktive Einheit ergibt 204 statt 404")
    void getActiveSession_none_returns204() throws Exception {
        // Arrange
        when(liveSessionService.getActiveSession(USER_A)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/v1/sessions/active").cookie(tokenCookie(USER_A)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("UT-12 Satz erfassen ergibt 201 mit der ganzen Übung")
    void addSet_valid_returns201() throws Exception {
        // Arrange
        when(liveSessionService.addSet(eq(USER_A), eq(101L), any(SetRequest.class))).thenReturn(SQUAT_EXERCISE);

        // Act & Assert
        mockMvc.perform(post("/api/v1/sessions/active/exercises/{sessionExerciseId}/sets", 101L)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"weightKg": 100.5, "repetitions": 5, "restSeconds": 180}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.sets", hasSize(1)))
                .andExpect(jsonPath("$.nextSet").exists());

        verify(liveSessionService).addSet(USER_A, 101L, new SetRequest(new BigDecimal("100.5"), 5, 180));
    }

    @Test
    @DisplayName("UT-12 Ungültige Satzwerte ergeben 400")
    void addSet_invalidValues_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/active/exercises/{sessionExerciseId}/sets", 101L)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"weightKg": 82.125, "repetitions": 0, "restSeconds": 3601}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasSize(3)))
                .andExpect(jsonPath("$.details", hasItem(startsWith("weightKg:"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("repetitions:"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("restSeconds:"))));

        verifyNoInteractions(liveSessionService);
    }

    @Test
    @DisplayName("UT-12 Satz ändern ergibt 200 mit der ganzen Übung")
    void updateSet_valid_returns200() throws Exception {
        // Arrange
        when(liveSessionService.updateSet(eq(USER_A), eq(501L), any(SetRequest.class))).thenReturn(SQUAT_EXERCISE);

        // Act & Assert
        mockMvc.perform(put("/api/v1/sessions/active/sets/{setId}", 501L)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"weightKg": 100, "repetitions": 6}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101));

        verify(liveSessionService).updateSet(USER_A, 501L, new SetRequest(new BigDecimal("100"), 6, null));
    }

    @Test
    @DisplayName("UT-12 Satz verschieben ergibt 200 mit der ganzen Übung")
    void moveSet_valid_returns200() throws Exception {
        // Arrange
        when(liveSessionService.moveSet(eq(USER_A), eq(501L), any(MoveSetRequest.class))).thenReturn(SQUAT_EXERCISE);

        // Act & Assert
        mockMvc.perform(put("/api/v1/sessions/active/sets/{setId}/position", 501L)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newSetNumber": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets[0].setNumber").value(1));

        verify(liveSessionService).moveSet(USER_A, 501L, new MoveSetRequest(1));
    }

    @Test
    @DisplayName("UT-12 Verschieben auf Satznummer 0 ergibt 400")
    void moveSet_zero_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/sessions/active/sets/{setId}/position", 501L)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newSetNumber": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasItem(startsWith("newSetNumber:"))));

        verifyNoInteractions(liveSessionService);
    }

    @Test
    @DisplayName("UT-12 Satz löschen ergibt 200 mit der ganzen Übung statt 204")
    void deleteSet_returns200WithExercise() throws Exception {
        // Arrange
        when(liveSessionService.deleteSet(USER_A, 501L)).thenReturn(SQUAT_EXERCISE);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/sessions/active/sets/{setId}", 501L).cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101));

        verify(liveSessionService).deleteSet(USER_A, 501L);
    }

    @Test
    @DisplayName("UT-14 Notiz setzen ergibt 204")
    void updateNote_valid_returns204() throws Exception {
        mockMvc.perform(put("/api/v1/sessions/active/note")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"note": "Gute Einheit"}
                                """))
                .andExpect(status().isNoContent());

        verify(liveSessionService).updateNote(USER_A, new NoteRequest("Gute Einheit"));
    }

    @Test
    @DisplayName("UT-14 Abschliessen und Abbrechen ergeben 204")
    void finishAndCancelSession_return204() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/active/finish").cookie(tokenCookie(USER_A)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mockMvc.perform(post("/api/v1/sessions/active/cancel").cookie(tokenCookie(USER_A)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(liveSessionService).finishSession(USER_A);
        verify(liveSessionService).cancelSession(USER_A);
    }

    @Test
    @DisplayName("UT-14 Abschliessen ohne Satz ergibt 409")
    void finishSession_withoutSets_returns409() throws Exception {
        // Arrange
        doThrow(new ConflictException(ErrorMessages.SESSION_WITHOUT_SETS)).when(liveSessionService).finishSession(USER_A);

        // Act & Assert
        mockMvc.perform(post("/api/v1/sessions/active/finish").cookie(tokenCookie(USER_A)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    private Cookie tokenCookie(Long userId) {
        when(userService.exists(userId)).thenReturn(true);
        return new Cookie(COOKIE_NAME, jwtCookieService.createAuthCookie(userId).getValue());
    }

}
