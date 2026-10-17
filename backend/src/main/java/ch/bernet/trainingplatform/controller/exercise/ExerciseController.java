package ch.bernet.trainingplatform.controller.exercise;

import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseRequest;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.exercise.dto.MuscleGroupResponse;
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

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public List<ExerciseResponse> getExercises(@AuthenticationPrincipal Long userId) {
        // exerciseService.getExercises aufrufen
        return exerciseService.getExercises(userId);
    }

    @GetMapping("/muscle-groups")
    public List<MuscleGroupResponse> getMuscleGroups() {
        // exerciseService.getMuscleGroups aufrufen
        return exerciseService.getMuscleGroups();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse createExercise(@AuthenticationPrincipal Long userId,
                                           @Valid @RequestBody ExerciseRequest request) {
        // exerciseService.createExercise aufrufen
        return exerciseService.createExercise(userId, request);
    }

    @PutMapping("/{exerciseId}")
    public ExerciseResponse updateExercise(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long exerciseId,
                                           @Valid @RequestBody ExerciseRequest request) {
        // exerciseService.updateExercise aufrufen
        return exerciseService.updateExercise(userId, exerciseId, request);
    }

    @DeleteMapping("/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@AuthenticationPrincipal Long userId, @PathVariable Long exerciseId) {
        // exerciseService.deleteExercise aufrufen
        exerciseService.deleteExercise(userId, exerciseId);
    }

}
