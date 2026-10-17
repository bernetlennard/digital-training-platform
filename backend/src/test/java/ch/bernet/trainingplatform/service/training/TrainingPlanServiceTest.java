package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.common.BusinessRuleException;
import ch.bernet.trainingplatform.common.NotFoundException;
import ch.bernet.trainingplatform.persistence.training.SessionStatus;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import ch.bernet.trainingplatform.service.training.dto.PlanSessionRequest;
import ch.bernet.trainingplatform.service.training.dto.PlannedSessionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceTest {

    private static final Long LEGS_ROUTINE_ID = 21L;
    private static final RoutineResponse PUSH = new RoutineResponse(ROUTINE_ID, "Push", List.of());
    private static final RoutineResponse LEGS = new RoutineResponse(LEGS_ROUTINE_ID, "Beine", List.of());

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private SessionAccess sessionAccess;

    private TrainingPlanService trainingPlanService;

    @BeforeEach
    void setUp() {
        trainingPlanService = new TrainingPlanService(trainingSessionRepository, sessionAccess, CLOCK);
    }

    @Test
    @DisplayName("UT-10 Geplante Einheiten werden nach Datum sortiert, überfällige zuerst")
    void getPlannedSessions_sortedByDateWithRoutineName() {
        // Arrange
        when(trainingSessionRepository.findByUserIdAndStatus(USER_A, SessionStatus.PLANNED)).thenReturn(List.of(
                plannedSession(31L, ROUTINE_ID, TODAY.plusDays(2)),
                plannedSession(32L, LEGS_ROUTINE_ID, TODAY.minusDays(1)),
                plannedSession(33L, ROUTINE_ID, TODAY)));
        when(sessionAccess.getRoutineSummaries(USER_A)).thenReturn(Map.of(
                ROUTINE_ID, new RoutineSummary(ROUTINE_ID, "Push", false),
                LEGS_ROUTINE_ID, new RoutineSummary(LEGS_ROUTINE_ID, "Beine", false)));

        // Act
        List<PlannedSessionResponse> response = trainingPlanService.getPlannedSessions(USER_A);

        // Assert (#FA-4.3, #NFA-4.1)
        assertThat(response).containsExactly(
                new PlannedSessionResponse(32L, LEGS_ROUTINE_ID, "Beine", TODAY.minusDays(1)),
                new PlannedSessionResponse(33L, ROUTINE_ID, "Push", TODAY),
                new PlannedSessionResponse(31L, ROUTINE_ID, "Push", TODAY.plusDays(2)));
    }

    @Test
    @DisplayName("UT-10 Einplanen ab heute speichert eine geplante Einheit")
    void planSession_todayOrLater_savesPlannedSession() {
        // Arrange
        when(sessionAccess.getActiveRoutine(USER_A, ROUTINE_ID)).thenReturn(PUSH);
        when(trainingSessionRepository.save(any(TrainingSession.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), SESSION_ID));

        // Act
        PlannedSessionResponse response = trainingPlanService.planSession(USER_A, new PlanSessionRequest(ROUTINE_ID, TODAY));

        // Assert
        ArgumentCaptor<TrainingSession> captor = ArgumentCaptor.forClass(TrainingSession.class);
        verify(trainingSessionRepository).save(captor.capture());
        TrainingSession saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(USER_A);
        assertThat(saved.getRoutineId()).isEqualTo(ROUTINE_ID);
        assertThat(saved.getPlannedDate()).isEqualTo(TODAY);
        assertThat(saved.getStatus()).isEqualTo(SessionStatus.PLANNED);
        assertThat(saved.getStartedAt()).isNull();
        assertThat(response).isEqualTo(new PlannedSessionResponse(SESSION_ID, ROUTINE_ID, "Push", TODAY));
    }

    @Test
    @DisplayName("UT-10 Einplanen in der Vergangenheit ergibt 422")
    void planSession_inPast_throwsBusinessRule() {
        // Act & Assert (#FA-4.2)
        assertThatThrownBy(() -> trainingPlanService.planSession(USER_A, new PlanSessionRequest(ROUTINE_ID, TODAY.minusDays(1))))
                .isInstanceOf(BusinessRuleException.class);

        verify(trainingSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-10 Heute richtet sich nach der Zeitzone Europe/Zurich")
    void planSession_usesZurichTimeZone() {
        // Arrange: in UTC ist noch der 15.10., in Zürich bereits der 16.10. um 00:30
        Clock afterMidnightInZurich = Clock.fixed(Instant.parse("2026-10-15T22:30:00Z"), ZONE);
        TrainingPlanService service = new TrainingPlanService(trainingSessionRepository, sessionAccess, afterMidnightInZurich);
        LocalDate tomorrow = TODAY.plusDays(1);
        when(sessionAccess.getActiveRoutine(USER_A, ROUTINE_ID)).thenReturn(PUSH);
        when(trainingSessionRepository.save(any(TrainingSession.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), SESSION_ID));

        // Act & Assert: der 15.10. ist in Zürich gestern
        assertThatThrownBy(() -> service.planSession(USER_A, new PlanSessionRequest(ROUTINE_ID, TODAY)))
                .isInstanceOf(BusinessRuleException.class);

        PlannedSessionResponse response = service.planSession(USER_A, new PlanSessionRequest(ROUTINE_ID, tomorrow));
        assertThat(response.plannedDate()).isEqualTo(tomorrow);
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Routine einplanen ergibt 404")
    void planSession_foreignRoutine_throwsNotFound() {
        // Arrange
        when(sessionAccess.getActiveRoutine(USER_A, ROUTINE_ID)).thenThrow(new NotFoundException("Die Routine wurde nicht gefunden."));

        // Act & Assert
        assertThatThrownBy(() -> trainingPlanService.planSession(USER_A, new PlanSessionRequest(ROUTINE_ID, TODAY)))
                .isInstanceOf(NotFoundException.class);

        verify(trainingSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-10 Geplante Einheit verschieben ändert Routine und Datum")
    void updatePlannedSession_valid_reschedules() {
        // Arrange
        TrainingSession session = plannedSession(SESSION_ID, ROUTINE_ID, TODAY.plusDays(1));
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));
        when(sessionAccess.getActiveRoutine(USER_A, LEGS_ROUTINE_ID)).thenReturn(LEGS);
        // Ob save aufgerufen wird, bleibt der Umsetzung überlassen
        lenient().when(trainingSessionRepository.save(any(TrainingSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PlannedSessionResponse response = trainingPlanService.updatePlannedSession(USER_A, SESSION_ID,
                new PlanSessionRequest(LEGS_ROUTINE_ID, TODAY.plusDays(3)));

        // Assert (#FA-4.4)
        assertThat(session.getRoutineId()).isEqualTo(LEGS_ROUTINE_ID);
        assertThat(session.getPlannedDate()).isEqualTo(TODAY.plusDays(3));
        assertThat(response).isEqualTo(new PlannedSessionResponse(SESSION_ID, LEGS_ROUTINE_ID, "Beine", TODAY.plusDays(3)));
    }

    @Test
    @DisplayName("UT-10 Verschieben in die Vergangenheit ergibt 422")
    void updatePlannedSession_newDateInPast_throwsBusinessRule() {
        // Arrange
        TrainingSession session = plannedSession(SESSION_ID, ROUTINE_ID, TODAY.plusDays(1));
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThatThrownBy(() -> trainingPlanService.updatePlannedSession(USER_A, SESSION_ID,
                new PlanSessionRequest(ROUTINE_ID, TODAY.minusDays(1))))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(session.getPlannedDate()).isEqualTo(TODAY.plusDays(1));
    }

    @Test
    @DisplayName("UT-10 Überfällige Einheit kann nicht mehr geändert werden")
    void updatePlannedSession_overdue_throwsBusinessRule() {
        // Arrange: gestern geplant und nicht durchgeführt
        TrainingSession session = plannedSession(SESSION_ID, ROUTINE_ID, TODAY.minusDays(1));
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));

        // Act & Assert: auch ein gültiges neues Datum ändert daran nichts
        assertThatThrownBy(() -> trainingPlanService.updatePlannedSession(USER_A, SESSION_ID,
                new PlanSessionRequest(LEGS_ROUTINE_ID, TODAY.plusDays(1))))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(session.getRoutineId()).isEqualTo(ROUTINE_ID);
        assertThat(session.getPlannedDate()).isEqualTo(TODAY.minusDays(1));
    }

    @Test
    @DisplayName("UT-10 Nur geplante Einheiten können geändert werden")
    void updatePlannedSession_notPlanned_throwsNotFound() {
        // Arrange: die Einheit läuft bereits, Einheit 99 gehört Sportler B oder existiert nicht
        TrainingSession session = plannedSession(SESSION_ID, ROUTINE_ID, TODAY);
        session.start(NOW, List.of(BENCH_PRESS));
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));
        when(trainingSessionRepository.findByIdAndUserId(99L, USER_A)).thenReturn(Optional.empty());
        lenient().when(sessionAccess.getActiveRoutine(USER_A, LEGS_ROUTINE_ID)).thenReturn(LEGS);

        // Act & Assert
        PlanSessionRequest request = new PlanSessionRequest(LEGS_ROUTINE_ID, TODAY.plusDays(1));
        assertThatThrownBy(() -> trainingPlanService.updatePlannedSession(USER_A, SESSION_ID, request))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> trainingPlanService.updatePlannedSession(USER_A, 99L, request))
                .isInstanceOf(NotFoundException.class);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getRoutineId()).isEqualTo(ROUTINE_ID);
    }

    @Test
    @DisplayName("UT-10 Geplante Einheit wird hart gelöscht")
    void deletePlannedSession_planned_deletesSession() {
        // Arrange
        TrainingSession session = plannedSession(SESSION_ID, ROUTINE_ID, TODAY.plusDays(1));
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));

        // Act
        trainingPlanService.deletePlannedSession(USER_A, SESSION_ID);

        // Assert (#FA-4.5)
        verify(trainingSessionRepository).delete(session);
    }

    @Test
    @DisplayName("UT-10 Überfällige Einheit kann noch gelöscht werden")
    void deletePlannedSession_overdue_deletesSession() {
        // Arrange
        TrainingSession session = plannedSession(SESSION_ID, ROUTINE_ID, TODAY.minusDays(3));
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));

        // Act
        trainingPlanService.deletePlannedSession(USER_A, SESSION_ID);

        // Assert
        verify(trainingSessionRepository).delete(session);
    }

    @Test
    @DisplayName("UT-10 Gestartete oder fremde Einheit kann nicht als geplante gelöscht werden")
    void deletePlannedSession_notPlanned_throwsNotFound() {
        // Arrange: eine aktive Einheit kann nur abgebrochen werden
        TrainingSession session = plannedSession(SESSION_ID, ROUTINE_ID, TODAY);
        session.start(NOW, List.of(BENCH_PRESS));
        when(trainingSessionRepository.findByIdAndUserId(SESSION_ID, USER_A)).thenReturn(Optional.of(session));
        when(trainingSessionRepository.findByIdAndUserId(99L, USER_A)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> trainingPlanService.deletePlannedSession(USER_A, SESSION_ID))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> trainingPlanService.deletePlannedSession(USER_A, 99L))
                .isInstanceOf(NotFoundException.class);

        verify(trainingSessionRepository, never()).delete(any());
        verify(trainingSessionRepository, never()).deleteById(any());
    }

    private static TrainingSession plannedSession(Long id, Long routineId, LocalDate plannedDate) {
        return withId(new TrainingSession(USER_A, routineId, plannedDate), id);
    }

}
