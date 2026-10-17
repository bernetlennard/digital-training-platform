package ch.bernet.trainingplatform.controller.exercise;

import ch.bernet.trainingplatform.common.ApiStatus;
import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseRequest;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.exercise.dto.MuscleGroupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Übungen")
@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @Operation(summary = "Eigene Übungen auflisten")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @GetMapping
    public List<ExerciseResponse> getExercises(@AuthenticationPrincipal Long userId) {
        // exerciseService.getExercises aufrufen
        return exerciseService.getExercises(userId);
    }

    @Operation(summary = "Muskelgruppen auflisten")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @GetMapping("/muscle-groups")
    public List<MuscleGroupResponse> getMuscleGroups() {
        // exerciseService.getMuscleGroups aufrufen
        return exerciseService.getMuscleGroups();
    }

    @Operation(summary = "Übung anlegen")
    @ApiResponse(responseCode = "201", description = ApiStatus.CREATED)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse createExercise(@AuthenticationPrincipal Long userId,
                                           @Valid @RequestBody ExerciseRequest request) {
        // exerciseService.createExercise aufrufen
        return exerciseService.createExercise(userId, request);
    }

    @Operation(summary = "Übung ändern")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @PutMapping("/{exerciseId}")
    public ExerciseResponse updateExercise(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long exerciseId,
                                           @Valid @RequestBody ExerciseRequest request) {
        // exerciseService.updateExercise aufrufen
        return exerciseService.updateExercise(userId, exerciseId, request);
    }

    @Operation(summary = "Übung löschen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @DeleteMapping("/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@AuthenticationPrincipal Long userId, @PathVariable Long exerciseId) {
        // exerciseService.deleteExercise aufrufen
        exerciseService.deleteExercise(userId, exerciseId);
    }

}
