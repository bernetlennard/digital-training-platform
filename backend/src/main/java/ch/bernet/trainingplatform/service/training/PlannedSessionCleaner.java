package ch.bernet.trainingplatform.service.training;

import ch.bernet.trainingplatform.persistence.training.TrainingSessionRepository;
import ch.bernet.trainingplatform.service.routine.RoutineDeletionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PlannedSessionCleaner implements RoutineDeletionListener {

    private static final Logger log = LoggerFactory.getLogger(PlannedSessionCleaner.class);

    private final TrainingSessionRepository trainingSessionRepository;

    public PlannedSessionCleaner(TrainingSessionRepository trainingSessionRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
    }

    @Override
    public void onRoutineDeleted(Long userId, Long routineId) {
        // geplante Einheiten der Routine löschen (deletePlannedByRoutineId)
        int deleted = trainingSessionRepository.deletePlannedByRoutineId(userId, routineId);
        log.info("{} planned sessions of routine {} deleted, user {}", deleted, routineId, userId);
    }

}
