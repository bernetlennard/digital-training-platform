package ch.bernet.trainingplatform.service.exercise;

import ch.bernet.trainingplatform.persistence.exercise.Exercise;
import ch.bernet.trainingplatform.persistence.exercise.ExerciseRepository;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseRequest;
import ch.bernet.trainingplatform.service.exercise.dto.ExerciseResponse;
import ch.bernet.trainingplatform.service.exercise.dto.MuscleGroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseUsageChecker exerciseUsageChecker;

    public ExerciseService(ExerciseRepository exerciseRepository, ExerciseUsageChecker exerciseUsageChecker) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseUsageChecker = exerciseUsageChecker;
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getExercises(Long userId) {
        // TODO: aktive Übungen des Benutzers laden und mappen
        return null;
    }

    public List<MuscleGroupResponse> getMuscleGroups() {
        // TODO: alle MuscleGroup-Werte mappen
        return null;
    }

    @Transactional
    public ExerciseResponse createExercise(Long userId, ExerciseRequest request) {
        // TODO: assertNameUnique, Muskelgruppe über fromId auflösen, speichern
        return null;
    }

    @Transactional
    public ExerciseResponse updateExercise(Long userId, Long exerciseId, ExerciseRequest request) {
        // TODO: findActiveExercise, assertNameUnique (eigene ID ausnehmen), Felder setzen
        return null;
    }

    @Transactional
    public void deleteExercise(Long userId, Long exerciseId) {
        // TODO: in aktiver Routine verwendet -> ConflictException (#FA-2.6), sonst Soft Delete
    }

    @Transactional(readOnly = true)
    public void assertActiveExercises(Long userId, Collection<Long> exerciseIds) {
        // TODO: alle IDs müssen aktive Übungen des Benutzers sein, sonst NotFoundException
    }

    @Transactional(readOnly = true)
    public Map<Long, ExerciseResponse> findByIds(Long userId, Collection<Long> exerciseIds) {
        // TODO: Übungen (auch gelöschte) des Benutzers laden, Map nach ID
        return null;
    }

    private Exercise findActiveExercise(Long userId, Long exerciseId) {
        // TODO: aktive Übung laden, sonst NotFoundException
        return null;
    }

    private void assertNameUnique(Long userId, String name, Long exerciseId) {
        // TODO: gleicher Name bei anderer aktiver Übung -> ConflictException (exerciseId darf null sein)
    }

}
