package ch.bernet.trainingplatform.service.exercise;

import ch.bernet.trainingplatform.persistence.exercise.ExerciseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.bernet.trainingplatform.TestData.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private ExerciseUsageChecker exerciseUsageChecker;

    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    @DisplayName("UT-06 Übung wird für den angemeldeten Sportler gespeichert")
    void createExercise_valid_savesForUser() {
    }

    @Test
    @DisplayName("UT-06 Gleicher Name ohne Gross-/Kleinschreibung ergibt 409")
    void createExercise_duplicateNameIgnoringCase_throwsConflict() {
    }

    @Test
    @DisplayName("UT-06 Eigener Name beim Ändern ergibt keinen Konflikt")
    void updateExercise_ownName_noConflict() {
    }

    @Test
    @DisplayName("UT-02 Fremde oder gelöschte Übung ergibt 404")
    void updateExercise_foreignOrDeleted_throwsNotFound() {
    }

    @Test
    @DisplayName("UT-07 Unbenutzte Übung wird soft gelöscht")
    void deleteExercise_unused_softDeletes() {
    }

    @Test
    @DisplayName("UT-07 Übung in einer nicht gelöschten Routine ergibt 409")
    void deleteExercise_usedInActiveRoutine_throwsConflict() {
    }

}
