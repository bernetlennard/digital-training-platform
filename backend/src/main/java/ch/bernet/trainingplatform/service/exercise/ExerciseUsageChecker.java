package ch.bernet.trainingplatform.service.exercise;

public interface ExerciseUsageChecker {

    boolean isUsedInActiveRoutine(Long userId, Long exerciseId);

}
