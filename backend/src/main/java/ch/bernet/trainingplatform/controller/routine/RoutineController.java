package ch.bernet.trainingplatform.controller.routine;

import ch.bernet.trainingplatform.service.routine.RoutineService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        // routineService.getRoutines aufrufen
        return routineService.getRoutines(userId);
    }

    @GetMapping("/{routineId}")
    public RoutineResponse getRoutine(@AuthenticationPrincipal Long userId, @PathVariable Long routineId) {
        // routineService.getRoutine aufrufen
        return routineService.getRoutine(userId, routineId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineResponse createRoutine(@AuthenticationPrincipal Long userId,
                                         @Valid @RequestBody RoutineRequest request) {
        // routineService.createRoutine aufrufen
        return routineService.createRoutine(userId, request);
    }

    @PutMapping("/{routineId}")
    public RoutineResponse updateRoutine(@AuthenticationPrincipal Long userId,
                                         @PathVariable Long routineId,
                                         @Valid @RequestBody RoutineRequest request) {
        // routineService.updateRoutine aufrufen
        return routineService.updateRoutine(userId, routineId, request);
    }

    @DeleteMapping("/{routineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoutine(@AuthenticationPrincipal Long userId, @PathVariable Long routineId) {
        // routineService.deleteRoutine aufrufen
        routineService.deleteRoutine(userId, routineId);
    }

}
