package ch.bernet.trainingplatform.controller.exercise;

import ch.bernet.trainingplatform.common.ConflictException;
import ch.bernet.trainingplatform.common.ErrorMessages;
import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.security.CookieBearerTokenResolver;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.security.SecurityConfig;
import ch.bernet.trainingplatform.security.SecurityErrorHandler;
import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseRequest;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.exercise.dto.MuscleGroupResponse;
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

@WebMvcTest(ExerciseController.class)
@Import({SecurityConfig.class, SecurityErrorHandler.class, CookieBearerTokenResolver.class, JwtCookieService.class})
class ExerciseControllerTest {

    private static final ExerciseResponse SQUAT_RESPONSE = new ExerciseResponse(SQUAT, "Kniebeuge", 7, "Beine", null);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ExerciseService exerciseService;

    @Test
    @DisplayName("UT-02 Übungen ohne Token ergeben 401")
    void getExercises_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/exercises"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(exerciseService);
    }

    @Test
    @DisplayName("UT-06 Übungen des Sportlers auflisten ergibt 200")
    void getExercises_returns200() throws Exception {
        // Arrange
        when(exerciseService.getExercises(USER_A)).thenReturn(List.of(SQUAT_RESPONSE));

        // Act & Assert
        mockMvc.perform(get("/api/v1/exercises").cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(SQUAT))
                .andExpect(jsonPath("$[0].name").value("Kniebeuge"))
                .andExpect(jsonPath("$[0].muscleGroupId").value(7))
                .andExpect(jsonPath("$[0].muscleGroupName").value("Beine"));
    }

    @Test
    @DisplayName("UT-06 Muskelgruppen auflisten ergibt 200")
    void getMuscleGroups_returns200() throws Exception {
        // Arrange
        when(exerciseService.getMuscleGroups()).thenReturn(List.of(new MuscleGroupResponse(7, "Beine")));

        // Act & Assert
        mockMvc.perform(get("/api/v1/exercises/muscle-groups").cookie(tokenCookie(USER_A)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("Beine"));
    }

    @Test
    @DisplayName("UT-06 Übung anlegen ergibt 201 mit der neuen Übung")
    void createExercise_valid_returns201() throws Exception {
        // Arrange
        when(exerciseService.createExercise(eq(USER_A), any(ExerciseRequest.class))).thenReturn(SQUAT_RESPONSE);

        // Act & Assert
        mockMvc.perform(post("/api/v1/exercises")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Kniebeuge", "muscleGroupId": 7}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SQUAT))
                .andExpect(jsonPath("$.name").value("Kniebeuge"));

        verify(exerciseService).createExercise(USER_A, new ExerciseRequest("Kniebeuge", 7, null));
    }

    @Test
    @DisplayName("UT-06 Übung ohne Name und Muskelgruppe ergibt 400 mit Feldfehlern")
    void createExercise_invalid_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/exercises")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "muscleGroupId": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details", hasItem(startsWith("name:"))))
                .andExpect(jsonPath("$.details", hasItem(startsWith("muscleGroupId:"))));

        verifyNoInteractions(exerciseService);
    }

    @Test
    @DisplayName("UT-06 Vergebener Name ergibt 409")
    void createExercise_duplicateName_returns409() throws Exception {
        // Arrange
        when(exerciseService.createExercise(eq(USER_A), any(ExerciseRequest.class)))
                .thenThrow(new ConflictException(ErrorMessages.NAME_ALREADY_IN_USE));

        // Act & Assert
        mockMvc.perform(post("/api/v1/exercises")
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Kniebeuge", "muscleGroupId": 7}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    @DisplayName("UT-06 Übung ändern ergibt 200")
    void updateExercise_valid_returns200() throws Exception {
        // Arrange
        when(exerciseService.updateExercise(eq(USER_A), eq(SQUAT), any(ExerciseRequest.class))).thenReturn(SQUAT_RESPONSE);

        // Act & Assert
        mockMvc.perform(put("/api/v1/exercises/{exerciseId}", SQUAT)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Kniebeuge", "muscleGroupId": 7}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SQUAT));

        verify(exerciseService).updateExercise(USER_A, SQUAT, new ExerciseRequest("Kniebeuge", 7, null));
    }

    @Test
    @DisplayName("UT-02 Fremde Übung ändern ergibt 404")
    void updateExercise_foreign_returns404() throws Exception {
        // Arrange: die Übung gehört Sportler B
        when(exerciseService.updateExercise(eq(USER_A), eq(PULL_UP), any(ExerciseRequest.class)))
                .thenThrow(new NotFoundException(ErrorMessages.EXERCISE_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(put("/api/v1/exercises/{exerciseId}", PULL_UP)
                        .cookie(tokenCookie(USER_A))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Klimmzug", "muscleGroupId": 2}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("UT-07 Übung löschen ergibt 204")
    void deleteExercise_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/exercises/{exerciseId}", SQUAT).cookie(tokenCookie(USER_A)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(exerciseService).deleteExercise(USER_A, SQUAT);
    }

    @Test
    @DisplayName("UT-07 Übung in einer Routine löschen ergibt 409")
    void deleteExercise_usedInRoutine_returns409() throws Exception {
        // Arrange
        doThrow(new ConflictException(ErrorMessages.EXERCISE_IN_USE))
                .when(exerciseService).deleteExercise(USER_A, SQUAT);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/exercises/{exerciseId}", SQUAT).cookie(tokenCookie(USER_A)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    private Cookie tokenCookie(Long userId) {
        when(userService.exists(userId)).thenReturn(true);
        return new Cookie(COOKIE_NAME, jwtCookieService.createAuthCookie(userId).getValue());
    }

}
