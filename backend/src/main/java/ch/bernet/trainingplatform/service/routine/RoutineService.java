package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.persistence.routine.Routine;
import ch.bernet.trainingplatform.persistence.routine.RoutineRepository;
import ch.bernet.trainingplatform.service.exercise.ExerciseService;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineResponse;
import ch.bernet.trainingplatform.service.routine.dto.RoutineSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ExerciseService exerciseService;
    private final RoutineDeletionListener routineDeletionListener;

    public RoutineService(RoutineRepository routineRepository,
                          ExerciseService exerciseService,
                          RoutineDeletionListener routineDeletionListener) {
        this.routineRepository = routineRepository;
        this.exerciseService = exerciseService;
        this.routineDeletionListener = routineDeletionListener;
    }

    @Transactional(readOnly = true)
    public List<RoutineResponse> getRoutines(Long userId) {
        // TODO: aktive Routinen laden, Übungen über exerciseService.findByIds auflösen, mappen
        return null;
    }

    @Transactional(readOnly = true)
    public RoutineResponse getRoutine(Long userId, Long routineId) {
        // TODO: findActiveRoutine, Übungen auflösen, mappen
        return null;
    }

    @Transactional
    public RoutineResponse createRoutine(Long userId, RoutineRequest request) {
        // TODO: assertNameUnique, assertValidExercises, Routine anlegen, replaceExercises, speichern
        return null;
    }

    @Transactional
    public RoutineResponse updateRoutine(Long userId, Long routineId, RoutineRequest request) {
        // TODO: findActiveRoutine, assertNameUnique (eigene ID ausnehmen), assertValidExercises, Name und Übungen ersetzen
        return null;
    }

    @Transactional
    public void deleteRoutine(Long userId, Long routineId) {
        // TODO: Soft Delete, danach routineDeletionListener.onRoutineDeleted
    }

    @Transactional(readOnly = true)
    public List<RoutineSummary> findAllSummaries(Long userId) {
        // TODO: alle Routinen (auch gelöschte) als RoutineSummary
        return null;
    }

    private Routine findActiveRoutine(Long userId, Long routineId) {
        // TODO: aktive Routine laden, sonst NotFoundException
        return null;
    }

    private void assertNameUnique(Long userId, String name, Long routineId) {
        // TODO: gleicher Name bei anderer aktiver Routine -> ConflictException (routineId darf null sein)
    }

    private void assertValidExercises(Long userId, List<Long> exerciseIds) {
        // TODO: keine Duplikate, exerciseService.assertActiveExercises
    }

}
