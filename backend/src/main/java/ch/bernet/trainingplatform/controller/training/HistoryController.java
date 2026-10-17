package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.common.ApiStatus;
import ch.bernet.trainingplatform.service.training.HistoryService;
import ch.bernet.trainingplatform.service.training.dto.HistoryRoutineResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Verlauf")
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Operation(summary = "Routinen mit abgeschlossenen Einheiten auflisten")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @GetMapping("/routines")
    public List<HistoryRoutineResponse> getHistoryRoutines(@AuthenticationPrincipal Long userId) {
        // TODO: historyService.getHistoryRoutines aufrufen
        return null;
    }

    @Operation(summary = "Abgeschlossene Einheiten einer Routine auflisten")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @GetMapping("/routines/{routineId}/sessions")
    public List<SessionSummaryResponse> getRoutineHistory(@AuthenticationPrincipal Long userId,
                                                          @PathVariable Long routineId) {
        // TODO: historyService.getRoutineHistory aufrufen
        return null;
    }

    @Operation(summary = "Abgeschlossene Einheit im Detail lesen")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @GetMapping("/sessions/{sessionId}")
    public SessionResponse getSessionDetail(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        // TODO: historyService.getSessionDetail aufrufen
        return null;
    }

    @Operation(summary = "Einheit aus dem Verlauf löschen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "404", description = ApiStatus.NOT_FOUND)
    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        // TODO: historyService.deleteSession aufrufen
    }

}
