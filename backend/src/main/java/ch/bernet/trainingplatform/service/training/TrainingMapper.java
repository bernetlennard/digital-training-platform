package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.ExerciseSet;
import ch.bernet.trainingplatform.persistence.training.SessionExercise;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import ch.bernet.trainingplatform.service.training.dto.*;

import java.util.List;

public final class TrainingMapper {

    private TrainingMapper() {
    }

    public static PlannedSessionResponse toPlanned(TrainingSession session, String routineName) {
        // TrainingSession auf PlannedSessionResponse abbilden
        return new PlannedSessionResponse(session.getId(), session.getRoutineId(), routineName, session.getPlannedDate());
    }

    public static SessionSummaryResponse toSummary(TrainingSession session) {
        // TrainingSession auf SessionSummaryResponse abbilden
        return new SessionSummaryResponse(session.getId(), session.getStartedAt(), session.getFinishedAt());
    }

    public static SetResponse toSet(ExerciseSet set) {
        // ExerciseSet auf SetResponse abbilden
        return new SetResponse(set.getId(), set.getSetNumber(), set.getWeightKg(), set.getRepetitions(), set.getRestSeconds());
    }

    public static SessionExerciseResponse toExercise(SessionExercise item, String name, List<SetSuggestion> suggestedSets) {
        // SessionExercise inkl. Sätze (toSet) und Vorschlägen für die offenen Sätze abbilden
        List<SetResponse> sets = item.getSets().stream().map(TrainingMapper::toSet).toList();
        return new SessionExerciseResponse(item.getId(), item.getExerciseId(), name, item.getPosition(), sets, suggestedSets);
    }

    public static SessionResponse toSession(TrainingSession session, RoutineSummary routine,
                                            List<SessionExerciseResponse> exercises) {
        // TrainingSession auf SessionResponse abbilden, status als Label
        return new SessionResponse(session.getId(),
                session.getRoutineId(),
                routine.name(),
                routine.deleted(),
                session.getStatus().getLabel(),
                session.getStartedAt(),
                session.getFinishedAt(),
                session.getNote(),
                exercises);
    }

}
