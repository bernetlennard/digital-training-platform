package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.ExerciseSet;
import ch.bernet.trainingplatform.persistence.training.SessionExercise;
import ch.bernet.trainingplatform.persistence.training.TrainingSession;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import ch.bernet.trainingplatform.service.training.dto.PlannedSessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionExerciseResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionResponse;
import ch.bernet.trainingplatform.service.training.dto.SessionSummaryResponse;
import ch.bernet.trainingplatform.service.training.dto.SetResponse;
import ch.bernet.trainingplatform.service.training.dto.SetSuggestion;

import java.util.List;

public final class TrainingMapper {

    private TrainingMapper() {
    }

    public static PlannedSessionResponse toPlanned(TrainingSession session, String routineName) {
        // TODO: TrainingSession auf PlannedSessionResponse abbilden
        return null;
    }

    public static SessionSummaryResponse toSummary(TrainingSession session) {
        // TODO: TrainingSession auf SessionSummaryResponse abbilden
        return null;
    }

    public static SetResponse toSet(ExerciseSet set) {
        // TODO: ExerciseSet auf SetResponse abbilden
        return null;
    }

    public static SessionExerciseResponse toExercise(SessionExercise item, String name, SetSuggestion next) {
        // TODO: SessionExercise inkl. Sätze (toSet) abbilden, next darf null sein
        return null;
    }

    public static SessionResponse toSession(TrainingSession session, RoutineSummary routine,
                                            List<SessionExerciseResponse> exercises) {
        // TODO: TrainingSession auf SessionResponse abbilden, status als Label
        return null;
    }

}
