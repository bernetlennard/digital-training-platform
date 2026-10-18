package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.common.ApiStatus;
import ch.bernet.trainingplatform.service.training.TrainingPlanService;
import ch.bernet.trainingplatform.service.training.dto.PlanSessionRequest;
import ch.bernet.trainingplatform.service.training.dto.PlannedSessionResponse;
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

@Tag(name = "Trainingsplanung")
@RestController
@RequestMapping(path = "/api/v{version}/sessions/planned", version = "1")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @Operation(summary = "Geplante Einheiten auflisten")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @GetMapping
    public List<PlannedSessionResponse> getPlannedSessions(@AuthenticationPrincipal Long userId) {
        return trainingPlanService.getPlannedSessions(userId);
    }

    @Operation(summary = "Einheit planen")
    @ApiResponse(responseCode = "201", description = ApiStatus.CREATED)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "422", description = ApiStatus.UNPROCESSABLE_CONTENT)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlannedSessionResponse planSession(@AuthenticationPrincipal Long userId,
                                              @Valid @RequestBody PlanSessionRequest request) {
        return trainingPlanService.planSession(userId, request);
    }

    @Operation(summary = "Geplante Einheit verschieben")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "422", description = ApiStatus.UNPROCESSABLE_CONTENT)
    @PutMapping("/{sessionId}")
    public PlannedSessionResponse updatePlannedSession(@AuthenticationPrincipal Long userId,
                                                       @PathVariable Long sessionId,
                                                       @Valid @RequestBody PlanSessionRequest request) {
        return trainingPlanService.updatePlannedSession(userId, sessionId, request);
    }

    @Operation(summary = "Geplante Einheit löschen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlannedSession(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        trainingPlanService.deletePlannedSession(userId, sessionId);
    }

}
