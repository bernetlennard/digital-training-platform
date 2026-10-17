package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.common.ApiStatus;
import ch.bernet.trainingplatform.service.training.LiveSessionService;
import ch.bernet.trainingplatform.service.training.dto.MoveSetRequest;
import ch.bernet.trainingplatform.service.training.dto.NoteRequest;
import ch.bernet.trainingplatform.service.training.dto.SessionExerciseResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SetRequest;
import ch.bernet.trainingplatform.service.training.dto.StartSessionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

// TODO: Pfade der einzelnen Endpunkte mit der API-Spezifikation abgleichen
@Tag(name = "Live-Training")
@RestController
@RequestMapping(path = "/api/v{version}/sessions", version = "1")
public class LiveSessionController {

    private final LiveSessionService liveSessionService;

    public LiveSessionController(LiveSessionService liveSessionService) {
        this.liveSessionService = liveSessionService;
    }

    @Operation(summary = "Geplante Einheit starten")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @PostMapping("/planned/{sessionId}/start")
    public SessionResponse startPlannedSession(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        // TODO: liveSessionService.startPlannedSession aufrufen
        return null;
    }

    @Operation(summary = "Spontane Einheit starten")
    @ApiResponse(responseCode = "201", description = ApiStatus.CREATED)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse startSpontaneousSession(@AuthenticationPrincipal Long userId,
                                                   @Valid @RequestBody StartSessionRequest request) {
        // TODO: liveSessionService.startSpontaneousSession aufrufen
        return null;
    }

    @Operation(summary = "Aktive Einheit lesen")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @GetMapping("/active")
    public ResponseEntity<SessionResponse> getActiveSession(@AuthenticationPrincipal Long userId) {
        // TODO: 200 mit Einheit oder 204, wenn keine aktive Einheit existiert
        return null;
    }

    @Operation(summary = "Notiz zur Einheit setzen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @PutMapping("/active/note")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNote(@AuthenticationPrincipal Long userId, @Valid @RequestBody NoteRequest request) {
        // TODO: liveSessionService.updateNote aufrufen
    }

    @Operation(summary = "Satz erfassen")
    @ApiResponse(responseCode = "201", description = ApiStatus.CREATED)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @PostMapping("/active/exercises/{sessionExerciseId}/sets")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionExerciseResponse addSet(@AuthenticationPrincipal Long userId,
                                          @PathVariable Long sessionExerciseId,
                                          @Valid @RequestBody SetRequest request) {
        // TODO: liveSessionService.addSet aufrufen
        return null;
    }

    @Operation(summary = "Satz ändern")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @PutMapping("/active/sets/{setId}")
    public SessionExerciseResponse updateSet(@AuthenticationPrincipal Long userId,
                                             @PathVariable Long setId,
                                             @Valid @RequestBody SetRequest request) {
        // TODO: liveSessionService.updateSet aufrufen
        return null;
    }

    @Operation(summary = "Satz verschieben")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "422", description = ApiStatus.UNPROCESSABLE_CONTENT)
    @PutMapping("/active/sets/{setId}/position")
    public SessionExerciseResponse moveSet(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long setId,
                                           @Valid @RequestBody MoveSetRequest request) {
        // TODO: liveSessionService.moveSet aufrufen
        return null;
    }

    @Operation(summary = "Satz löschen")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @DeleteMapping("/active/sets/{setId}")
    public SessionExerciseResponse deleteSet(@AuthenticationPrincipal Long userId, @PathVariable Long setId) {
        // TODO: liveSessionService.deleteSet aufrufen
        return null;
    }

    @Operation(summary = "Einheit abschliessen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @PostMapping("/active/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishSession(@AuthenticationPrincipal Long userId) {
        // TODO: liveSessionService.finishSession aufrufen
    }

    @Operation(summary = "Einheit abbrechen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @PostMapping("/active/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSession(@AuthenticationPrincipal Long userId) {
        // TODO: liveSessionService.cancelSession aufrufen
    }

}
