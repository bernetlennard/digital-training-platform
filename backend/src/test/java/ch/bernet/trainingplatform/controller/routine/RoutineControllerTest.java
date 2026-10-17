package ch.bernet.trainingplatform.controller.routine;

import ch.bernet.trainingplatform.common.BusinessRuleException;
import ch.bernet.trainingplatform.common.ErrorMessages;
import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.security.CookieBearerTokenResolver;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.security.SecurityConfig;
import ch.bernet.trainingplatform.security.SecurityErrorHandler;
import ch.bernet.trainingplatform.service.routine.RoutineService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineExerciseResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
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

@WebMvcTest(RoutineController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class RoutineControllerTest {

    private static final RoutineResponse PUSH = new RoutineResponse(ROUTINE_ID, "Push", List.of(
            new RoutineExerciseResponse(1, SQUAT, "Kniebeuge", "Beine"),
            new RoutineExerciseResponse(2, PULL_UP, "Klimmzug", "Ruecken")));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RoutineService routineService;

    @Test
    @DisplayName("UT-02 Routinen ohne Token ergeben 401")
    void getRoutines_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/routines"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(routineService);
    }

    @Test
    @DisplayName("UT-08 Routinen auflisten ergibt 200 mit den Übungen in Reihenfolge")
    void getRoutines_returns200() throws Exception {
        // Arrange
        when(routineService.getRoutines(USER_A)).thenReturn(List.of(PUSH));

        // Act & Assert
        mockMvc.perform(get("/api/v1/routines").cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ROUTINE_ID))
                .andExpect(jsonPath("$[0].exercises[0].position").value(1))
                .andExpect(jsonPath("$[0].exercises[0].exerciseId").value(SQUAT))
                .andExpect(jsonPath("$[0].exercises[0].name").value("Kniebeuge"))
                .andExpect(jsonPath("$[0].exercises[1].position").value(2));
    }

    @Test
    @DisplayName("UT-08 Routine lesen ergibt 200")
    void getRoutine_returns200() throws Exception {
        // Arrange
        when(routineService.getRoutine(USER_A, ROUTINE_ID)).thenReturn(PUSH);

        // Act & Assert
        mockMvc.perform(get("/api/v1/routines/{routineId}", ROUTINE_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Push"));
    }

    @Test
    @DisplayName("UT-02 Fremde Routine lesen ergibt 404")
    void getRoutine_foreign_returns404() throws Exception {
        // Arrange: die Routine gehört Sportler B
        when(routineService.getRoutine(USER_A, 21L)).thenThrow(new NotFoundException(ErrorMessages.ROUTINE_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get("/api/v1/routines/{routineId}", 21L).cookie(tokenCookie(USER_A)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("UT-08 Routine anlegen ergibt 201")
    void createRoutine_valid_returns201() throws Exception {
        // Arrange
        when(routineService.createRoutine(eq(USER_A), any(RoutineRequest.class))).thenReturn(PUSH);

        // Act & Assert
        mockMvc.perform(post("/api/v1/routines")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Push", "exerciseIds": [%d, %d]}
                                """.formatted(SQUAT, PULL_UP)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ROUTINE_ID));

        // Die Reihenfolge im Request bestimmt die Positionen
        verify(routineService).createRoutine(USER_A, new RoutineRequest("Push", List.of(SQUAT, PULL_UP)));
    }

    @Test
    @DisplayName("UT-08 Routine ohne Übung ergibt 400")
    void createRoutine_withoutExercises_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/routines")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Push", "exerciseIds": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details", hasItem(startsWith("exerciseIds:"))));

        verifyNoInteractions(routineService);
    }

    @Test
    @DisplayName("UT-08 Doppelte Übung ergibt 422")
    void createRoutine_duplicateExercise_returns422() throws Exception {
        // Arrange
        when(routineService.createRoutine(eq(USER_A), any(RoutineRequest.class)))
                .thenThrow(new BusinessRuleException(ErrorMessages.DUPLICATE_EXERCISE_IDS));

        // Act & Assert
        mockMvc.perform(post("/api/v1/routines")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Push", "exerciseIds": [%d, %d]}
                                """.formatted(SQUAT, SQUAT)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("UT-08 Routine ändern ergibt 200")
    void updateRoutine_valid_returns200() throws Exception {
        // Arrange
        when(routineService.updateRoutine(eq(USER_A), eq(ROUTINE_ID), any(RoutineRequest.class))).thenReturn(PUSH);

        // Act & Assert
        mockMvc.perform(put("/api/v1/routines/{routineId}", ROUTINE_ID)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Push", "exerciseIds": [%d]}
                                """.formatted(PULL_UP)))
                .andExpect(status().isOk());

        verify(routineService).updateRoutine(USER_A, ROUTINE_ID, new RoutineRequest("Push", List.of(PULL_UP)));
    }

    @Test
    @DisplayName("UT-09 Routine löschen ergibt 204")
    void deleteRoutine_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/routines/{routineId}", ROUTINE_ID).cookie(tokenCookie(USER_A)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(routineService).deleteRoutine(USER_A, ROUTINE_ID);
    }

    private Cookie tokenCookie(Long userId) {
        when(userService.exists(userId)).thenReturn(true);
        return new Cookie(COOKIE_NAME, jwtCookieService.createAuthCookie(userId).getValue());
    }

}
