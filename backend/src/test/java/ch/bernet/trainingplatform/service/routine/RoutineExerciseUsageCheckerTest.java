package ch.bernet.trainingplatform.service.routine;

import ch.bernet.trainingplatform.persistence.routine.RoutineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutineExerciseUsageCheckerTest {

    @Mock
    private RoutineRepository routineRepository;

    @InjectMocks
    private RoutineExerciseUsageChecker routineExerciseUsageChecker;

    @Test
    @DisplayName("UT-07 Übung in einer nicht gelöschten Routine gilt als verwendet")
    void isUsedInActiveRoutine_usesActiveRoutinesOnly() {
        // Arrange: gelöschte Routinen filtert die Abfrage heraus (#FA-2.6)
        when(routineRepository.existsActiveWithExercise(USER_A, BENCH_PRESS)).thenReturn(true);
        when(routineRepository.existsActiveWithExercise(USER_A, SQUAT)).thenReturn(false);

        // Act & Assert
        assertThat(routineExerciseUsageChecker.isUsedInActiveRoutine(USER_A, BENCH_PRESS)).isTrue();
        assertThat(routineExerciseUsageChecker.isUsedInActiveRoutine(USER_A, SQUAT)).isFalse();
    }

}
