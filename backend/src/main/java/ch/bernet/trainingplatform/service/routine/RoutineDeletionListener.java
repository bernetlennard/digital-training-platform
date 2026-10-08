package ch.bernet.trainingplatform.service.routine;

public interface RoutineDeletionListener {

    void onRoutineDeleted(Long userId, Long routineId);

}
