package ch.bernet.trainingplatform.controller.routine;

import ch.bernet.trainingplatform.service.routine.RoutineService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
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
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @GetMapping
    public List<RoutineResponse> getRoutines(@AuthenticationPrincipal Long userId) {
        // TODO: routineService.getRoutines aufrufen
        return null;
    }

    @GetMapping("/{routineId}")
    public RoutineResponse getRoutine(@AuthenticationPrincipal Long userId, @PathVariable Long routineId) {
        // TODO: routineService.getRoutine aufrufen
        return null;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineResponse createRoutine(@AuthenticationPrincipal Long userId,
                                         @Valid @RequestBody RoutineRequest request) {
        // TODO: routineService.createRoutine aufrufen
        return null;
    }

    @PutMapping("/{routineId}")
    public RoutineResponse updateRoutine(@AuthenticationPrincipal Long userId,
                                         @PathVariable Long routineId,
                                         @Valid @RequestBody RoutineRequest request) {
        // TODO: routineService.updateRoutine aufrufen
        return null;
    }

    @DeleteMapping("/{routineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoutine(@AuthenticationPrincipal Long userId, @PathVariable Long routineId) {
        // TODO: routineService.deleteRoutine aufrufen
    }

}
