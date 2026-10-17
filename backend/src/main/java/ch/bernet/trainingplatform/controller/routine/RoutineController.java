package ch.bernet.trainingplatform.controller.routine;

import ch.bernet.trainingplatform.common.ApiStatus;
import ch.bernet.trainingplatform.service.routine.RoutineService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Routinen")
@RestController
@RequestMapping("/api/routines")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @Operation(summary = "Eigene Routinen auflisten")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @GetMapping
    public List<RoutineResponse> getRoutines(@AuthenticationPrincipal Long userId) {
        // routineService.getRoutines aufrufen
        return routineService.getRoutines(userId);
    }

    @Operation(summary = "Routine mit Übungen lesen")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @GetMapping("/{routineId}")
    public RoutineResponse getRoutine(@AuthenticationPrincipal Long userId, @PathVariable Long routineId) {
        // routineService.getRoutine aufrufen
        return routineService.getRoutine(userId, routineId);
    }

    @Operation(summary = "Routine anlegen")
    @ApiResponse(responseCode = "201", description = ApiStatus.CREATED)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @ApiResponse(responseCode = "422", description = ApiStatus.UNPROCESSABLE_CONTENT)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineResponse createRoutine(@AuthenticationPrincipal Long userId,
                                         @Valid @RequestBody RoutineRequest request) {
        // routineService.createRoutine aufrufen
        return routineService.createRoutine(userId, request);
    }

    @Operation(summary = "Routine ändern")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @ApiResponse(responseCode = "422", description = ApiStatus.UNPROCESSABLE_CONTENT)
    @PutMapping("/{routineId}")
    public RoutineResponse updateRoutine(@AuthenticationPrincipal Long userId,
                                         @PathVariable Long routineId,
                                         @Valid @RequestBody RoutineRequest request) {
        // routineService.updateRoutine aufrufen
        return routineService.updateRoutine(userId, routineId, request);
    }

    @Operation(summary = "Routine löschen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @DeleteMapping("/{routineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoutine(@AuthenticationPrincipal Long userId, @PathVariable Long routineId) {
        // routineService.deleteRoutine aufrufen
        routineService.deleteRoutine(userId, routineId);
    }

}
