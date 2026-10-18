package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.persistence.routine.RoutineRepository;
import ch.bernet.trainingplatform.service.exercise.ExerciseUsageChecker;
import org.springframework.stereotype.Component;

@Component
public class RoutineExerciseUsageChecker implements ExerciseUsageChecker {

    private final RoutineRepository routineRepository;

    public RoutineExerciseUsageChecker(RoutineRepository routineRepository) {
        this.routineRepository = routineRepository;
    }

    @Override
    public boolean isUsedInActiveRoutine(Long userId, Long exerciseId) {
        // routineRepository.existsActiveWithExercise verwenden
        return routineRepository.existsActiveWithExercise(userId, exerciseId);
    }

}
