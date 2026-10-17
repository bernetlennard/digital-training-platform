package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.common.BusinessRuleException;
import ch.bernet.trainingplatform.common.ErrorMessages;
import ch.bernet.trainingplatform.security.CookieBearerTokenResolver;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.security.SecurityConfig;
import ch.bernet.trainingplatform.security.SecurityErrorHandler;
import ch.bernet.trainingplatform.service.training.TrainingPlanService;
import ch.bernet.trainingplatform.service.training.dto.PlanSessionRequest;
import ch.bernet.trainingplatform.service.training.dto.PlannedSessionResponse;
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

import java.util.List;

import static ch.bernet.trainingplatform.TestData.*;
import static ch.bernet.trainingplatform.security.JwtCookieService.COOKIE_NAME;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(TrainingPlanController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class TrainingPlanControllerTest {

    private static final PlannedSessionResponse PLANNED = new PlannedSessionResponse(SESSION_ID, ROUTINE_ID, "Push", TODAY);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TrainingPlanService trainingPlanService;

    @Test
    @DisplayName("UT-02 Geplante Einheiten ohne Token ergeben 401")
    void getPlannedSessions_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/sessions/planned"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(trainingPlanService);
    }

    @Test
    @DisplayName("UT-10 Geplante Einheiten auflisten ergibt 200 mit Datum nach ISO 8601")
    void getPlannedSessions_returns200() throws Exception {
        // Arrange
        when(trainingPlanService.getPlannedSessions(USER_A)).thenReturn(List.of(PLANNED));

        // Act & Assert
        mockMvc.perform(get("/api/sessions/planned").cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SESSION_ID))
                .andExpect(jsonPath("$[0].routineId").value(ROUTINE_ID))
                .andExpect(jsonPath("$[0].routineName").value("Push"))
                .andExpect(jsonPath("$[0].plannedDate").value("2026-10-15"));
    }

    @Test
    @DisplayName("UT-10 Einheit planen ergibt 201")
    void planSession_valid_returns201() throws Exception {
        // Arrange
        when(trainingPlanService.planSession(eq(USER_A), any(PlanSessionRequest.class))).thenReturn(PLANNED);

        // Act & Assert
        mockMvc.perform(post("/api/sessions/planned")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"routineId": %d, "plannedDate": "2026-10-15"}
                                """.formatted(ROUTINE_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SESSION_ID));

        verify(trainingPlanService).planSession(USER_A, new PlanSessionRequest(ROUTINE_ID, TODAY));
    }

    @Test
    @DisplayName("UT-10 Planung ohne Routine und Datum ergibt 400")
    void planSession_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/sessions/planned")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details", hasItem(startsWith("routineId:"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("plannedDate:"))));

        verifyNoInteractions(trainingPlanService);
    }

    @Test
    @DisplayName("UT-10 Datum in der Vergangenheit ergibt 422")
    void planSession_inPast_returns422() throws Exception {
        // Arrange
        when(trainingPlanService.planSession(eq(USER_A), any(PlanSessionRequest.class)))
                .thenThrow(new BusinessRuleException(ErrorMessages.PLANNED_DATE_IN_PAST));

        // Act & Assert
        mockMvc.perform(post("/api/sessions/planned")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"routineId": %d, "plannedDate": "2026-10-14"}
                                """.formatted(ROUTINE_ID)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("UT-10 Geplante Einheit verschieben ergibt 200")
    void updatePlannedSession_valid_returns200() throws Exception {
        // Arrange
        when(trainingPlanService.updatePlannedSession(eq(USER_A), eq(SESSION_ID), any(PlanSessionRequest.class)))
                .thenReturn(new PlannedSessionResponse(SESSION_ID, ROUTINE_ID, "Push", TODAY.plusDays(2)));

        // Act & Assert
        mockMvc.perform(put("/api/sessions/planned/{sessionId}", SESSION_ID)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"routineId": %d, "plannedDate": "2026-10-17"}
                                """.formatted(ROUTINE_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedDate").value("2026-10-17"));

        verify(trainingPlanService).updatePlannedSession(USER_A, SESSION_ID, new PlanSessionRequest(ROUTINE_ID, TODAY.plusDays(2)));
    }

    @Test
    @DisplayName("UT-10 Geplante Einheit löschen ergibt 204")
    void deletePlannedSession_returns204() throws Exception {
        mockMvc.perform(delete("/api/sessions/planned/{sessionId}", SESSION_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(trainingPlanService).deletePlannedSession(USER_A, SESSION_ID);
    }

    private Cookie tokenCookie(Long userId) {
        when(userService.exists(userId)).thenReturn(true);
        return new Cookie(COOKIE_NAME, jwtCookieService.createAuthCookie(userId).getValue());
    }

}
