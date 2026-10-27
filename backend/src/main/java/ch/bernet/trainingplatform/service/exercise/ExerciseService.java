package ch.bernet.trainingplatform.service.exercise;

import ch.bernet.trainingplatform.common.exception.ConflictException;
import ch.bernet.trainingplatform.common.exception.ErrorMessages;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import ch.bernet.trainingplatform.persistence.exercise.Exercise;
import ch.bernet.trainingplatform.persistence.exercise.ExerciseRepository;
import ch.bernet.trainingplatform.persistence.exercise.MuscleGroup;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseRequest;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.exercise.dto.MuscleGroupResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExerciseService {

    private static final Logger log = LoggerFactory.getLogger(ExerciseService.class);

    private final ExerciseRepository exerciseRepository;
    private final ExerciseUsageChecker exerciseUsageChecker;

    public ExerciseService(ExerciseRepository exerciseRepository, ExerciseUsageChecker exerciseUsageChecker) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseUsageChecker = exerciseUsageChecker;
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercises(Long userId) {
        // aktive Übungen des Benutzers laden und mappen
        return exerciseRepository.findActiveByUserId(userId).stream()
                .map(ExerciseMapper::toResponse)
                .toList();
    }

    public List<MuscleGroupResponse> getMuscleGroups() {
        // alle MuscleGroup-Werte mappen
        return Stream.of(MuscleGroup.values()).map(ExerciseMapper::toResponse).toList();
    }

    @Transactional
    public ExerciseResponse createExercise(Long userId, ExerciseRequest request) {
        // assertNameUnique, Muskelgruppe über fromId auflösen, speichern
        assertNameUnique(userId, request.name(), null);

        MuscleGroup muscleGroup = MuscleGroup.fromId(request.muscleGroupId());
        Exercise exercise = new Exercise(userId, request.name(), muscleGroup, request.description());

        exerciseRepository.save(exercise);
        log.info("Exercise {} created, user {}", exercise.getId(), userId);
        return ExerciseMapper.toResponse(exercise);
    }

    @Transactional
    public ExerciseResponse updateExercise(Long userId, Long exerciseId, ExerciseRequest request) {
        // findActiveExercise, assertNameUnique (eigene ID ausnehmen), Felder setzen
        Exercise exercise = findActiveExercise(userId, exerciseId);
        assertNameUnique(userId, request.name(), exerciseId);
        exercise.setName(request.name());
        exercise.setMuscleGroup(MuscleGroup.fromId(request.muscleGroupId()));
        exercise.setDescription(request.description());

        exerciseRepository.save(exercise);
        log.info("Exercise {} updated, user {}", exerciseId, userId);
        return ExerciseMapper.toResponse(exercise);
    }

    @Transactional
    public void deleteExercise(Long userId, Long exerciseId) {
        // in aktiver Routine verwendet -> ConflictException, sonst Soft Delete
        Exercise exercise = findActiveExercise(userId, exerciseId);
        if (exerciseUsageChecker.isUsedInActiveRoutine(userId, exerciseId)) {
            throw new ConflictException(ErrorMessages.EXERCISE_IN_USE);
        }
        exercise.setDeleted(true);
        exerciseRepository.save(exercise);
        log.info("Exercise {} deleted, user {}", exerciseId, userId);
    }

    @Transactional(readOnly = true)
    public void assertActiveExercises(Long userId, Collection<Long> exerciseIds) {
        // alle IDs müssen aktive Übungen des Benutzers sein, sonst NotFoundException
        Set<Long> requestedIds = new HashSet<>(exerciseIds);
        Set<Long> activeIds = exerciseRepository.findAllByIdInAndUserId(requestedIds, userId).stream()
                .filter(exercise -> !exercise.isDeleted())
                .map(Exercise::getId)
                .collect(Collectors.toSet());

        if (!activeIds.equals(requestedIds)) {
            throw new NotFoundException(ErrorMessages.EXERCISE_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, ExerciseResponse> findByIds(Long userId, Collection<Long> exerciseIds) {
        // Übungen (auch gelöschte) des Benutzers laden, Map nach ID
        return exerciseRepository.findAllByIdInAndUserId(exerciseIds, userId).stream()
                .collect(Collectors.toMap(Exercise::getId, ExerciseMapper::toResponse));
    }

    private Exercise findActiveExercise(Long userId, Long exerciseId) {
        // aktive Übung laden, sonst NotFoundException
        return exerciseRepository.findActiveByIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.EXERCISE_NOT_FOUND));
    }

    private void assertNameUnique(Long userId, String name, Long exerciseId) {
        // gleicher Name bei anderer aktiver Übung -> ConflictException (exerciseId darf null sein)
        exerciseRepository.findActiveByUserIdAndName(userId, name)
                .filter(existing -> !existing.getId().equals(exerciseId))
                .ifPresent(existing -> {
                    throw new ConflictException(ErrorMessages.NAME_ALREADY_IN_USE);
                });
    }

}
