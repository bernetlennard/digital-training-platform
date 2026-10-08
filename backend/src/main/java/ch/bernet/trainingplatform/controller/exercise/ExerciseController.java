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
        // TODO: exerciseService.getExercises aufrufen
        return null;
    }

    @GetMapping("/muscle-groups")
    public List<MuscleGroupResponse> getMuscleGroups() {
        // TODO: exerciseService.getMuscleGroups aufrufen
        return null;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse createExercise(@AuthenticationPrincipal Long userId,
                                           @Valid @RequestBody ExerciseRequest request) {
        // TODO: exerciseService.createExercise aufrufen
        return null;
    }

    @PutMapping("/{exerciseId}")
    public ExerciseResponse updateExercise(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long exerciseId,
                                           @Valid @RequestBody ExerciseRequest request) {
        // TODO: exerciseService.updateExercise aufrufen
        return null;
    }

    @DeleteMapping("/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@AuthenticationPrincipal Long userId, @PathVariable Long exerciseId) {
        // TODO: exerciseService.deleteExercise aufrufen
    }

}
