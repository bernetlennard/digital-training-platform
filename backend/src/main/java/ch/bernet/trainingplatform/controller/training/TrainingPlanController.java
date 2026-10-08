package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.service.training.TrainingPlanService;
import ch.bernet.trainingplatform.service.training.dto.PlanSessionRequest;
import ch.bernet.trainingplatform.service.training.dto.PlannedSessionResponse;
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
@RequestMapping("/api/sessions/planned")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @GetMapping
    public List<PlannedSessionResponse> getPlannedSessions(@AuthenticationPrincipal Long userId) {
        // TODO: trainingPlanService.getPlannedSessions aufrufen
        return null;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlannedSessionResponse planSession(@AuthenticationPrincipal Long userId,
                                              @Valid @RequestBody PlanSessionRequest request) {
        // TODO: trainingPlanService.planSession aufrufen
        return null;
    }

    @PutMapping("/{sessionId}")
    public PlannedSessionResponse updatePlannedSession(@AuthenticationPrincipal Long userId,
                                                       @PathVariable Long sessionId,
                                                       @Valid @RequestBody PlanSessionRequest request) {
        // TODO: trainingPlanService.updatePlannedSession aufrufen
        return null;
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlannedSession(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        // TODO: trainingPlanService.deletePlannedSession aufrufen
    }

}
