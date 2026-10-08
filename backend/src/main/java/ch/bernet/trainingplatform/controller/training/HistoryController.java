package ch.bernet.trainingplatform.controller.training;

import ch.bernet.trainingplatform.service.training.HistoryService;
import ch.bernet.trainingplatform.service.training.dto.HistoryRoutineResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/routines")
    public List<HistoryRoutineResponse> getHistoryRoutines(@AuthenticationPrincipal Long userId) {
        // TODO: historyService.getHistoryRoutines aufrufen
        return null;
    }

    @GetMapping("/routines/{routineId}/sessions")
    public List<SessionSummaryResponse> getRoutineHistory(@AuthenticationPrincipal Long userId,
                                                          @PathVariable Long routineId) {
        // TODO: historyService.getRoutineHistory aufrufen
        return null;
    }

    @GetMapping("/sessions/{sessionId}")
    public SessionResponse getSessionDetail(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        // TODO: historyService.getSessionDetail aufrufen
        return null;
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@AuthenticationPrincipal Long userId, @PathVariable Long sessionId) {
        // TODO: historyService.deleteSession aufrufen
    }

}
