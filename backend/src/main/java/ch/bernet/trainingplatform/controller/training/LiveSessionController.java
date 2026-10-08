package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.service.training.LiveSessionService;
import ch.bernet.trainingplatform.service.training.dto.MoveSetRequest;
import ch.bernet.trainingplatform.service.training.dto.NoteRequest;
import ch.bernet.trainingplatform.service.training.dto.SessionExerciseResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SetRequest;
import ch.bernet.trainingplatform.service.training.dto.StartSessionRequest;
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
@RestController
@RequestMapping("/api/sessions")
public class LiveSessionController {

    private final LiveSessionService liveSessionService;

    public LiveSessionController(LiveSessionService liveSessionService) {
        this.liveSessionService = liveSessionService;
    }

    @PostMapping("/planned/{sessionId}/start")
    public SessionResponse startPlannedSession(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        // TODO: liveSessionService.startPlannedSession aufrufen
        return null;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse startSpontaneousSession(@AuthenticationPrincipal Long userId,
                                                   @Valid @RequestBody StartSessionRequest request) {
        // TODO: liveSessionService.startSpontaneousSession aufrufen
        return null;
    }

    @GetMapping("/active")
    public ResponseEntity<SessionResponse> getActiveSession(@AuthenticationPrincipal Long userId) {
        // TODO: 200 mit Einheit oder 204, wenn keine aktive Einheit existiert
        return null;
    }

    @PutMapping("/active/note")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNote(@AuthenticationPrincipal Long userId, @Valid @RequestBody NoteRequest request) {
        // TODO: liveSessionService.updateNote aufrufen
    }

    @PostMapping("/active/exercises/{sessionExerciseId}/sets")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionExerciseResponse addSet(@AuthenticationPrincipal Long userId,
                                          @PathVariable Long sessionExerciseId,
                                          @Valid @RequestBody SetRequest request) {
        // TODO: liveSessionService.addSet aufrufen
        return null;
    }

    @PutMapping("/active/sets/{setId}")
    public SessionExerciseResponse updateSet(@AuthenticationPrincipal Long userId,
                                             @PathVariable Long setId,
                                             @Valid @RequestBody SetRequest request) {
        // TODO: liveSessionService.updateSet aufrufen
        return null;
    }

    @PutMapping("/active/sets/{setId}/position")
    public SessionExerciseResponse moveSet(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long setId,
                                           @Valid @RequestBody MoveSetRequest request) {
        // TODO: liveSessionService.moveSet aufrufen
        return null;
    }

    @DeleteMapping("/active/sets/{setId}")
    public SessionExerciseResponse deleteSet(@AuthenticationPrincipal Long userId, @PathVariable Long setId) {
        // TODO: liveSessionService.deleteSet aufrufen
        return null;
    }

    @PostMapping("/active/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishSession(@AuthenticationPrincipal Long userId) {
        // TODO: liveSessionService.finishSession aufrufen
    }

    @PostMapping("/active/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSession(@AuthenticationPrincipal Long userId) {
        // TODO: liveSessionService.cancelSession aufrufen
    }

}
