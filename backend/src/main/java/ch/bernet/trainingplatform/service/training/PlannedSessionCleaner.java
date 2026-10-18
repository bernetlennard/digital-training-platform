package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.routine.RoutineDeletionListener;
import org.springframework.stereotype.Component;

@Component
public class PlannedSessionCleaner implements RoutineDeletionListener {

    private final TrainingSessionRepository trainingSessionRepository;

    public PlannedSessionCleaner(TrainingSessionRepository trainingSessionRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
    }

    @Override
    public void onRoutineDeleted(Long userId, Long routineId) {
        // geplante Einheiten der Routine löschen (deletePlannedByRoutineId)
        trainingSessionRepository.deletePlannedByRoutineId(userId, routineId);
    }

}
