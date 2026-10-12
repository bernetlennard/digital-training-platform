package ch.bernet.trainingplatform.persistence.training;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ch.bernet.trainingplatform.TestData.*;

class TrainingSessionTest {

    @Test
    @DisplayName("UT-11 Geplante Einheit starten setzt Status aktiv und kopiert die Übungen")
    void start_planned_becomesActiveAndCopiesExercises() {
    }

    @Test
    @DisplayName("UT-11 Nur eine geplante Einheit kann gestartet werden")
    void start_notPlanned_throwsException() {
    }

    @Test
    @DisplayName("UT-09 Gestartete Einheit behält ihre Übungen, wenn die Routine ändert")
    void start_copiesExercises_routineChangeHasNoEffect() {
    }

    @Test
    @DisplayName("UT-10 Geplante Einheit verschieben ändert Routine und Datum")
    void reschedule_planned_updatesRoutineAndDate() {
    }

    @Test
    @DisplayName("UT-10 Nur geplante Einheiten können verschoben werden")
    void reschedule_notPlanned_throwsException() {
    }

    @Test
    @DisplayName("UT-14 Abschliessen setzt den Status und entfernt Übungen ohne Satz")
    void finish_withSets_completesAndRemovesExercisesWithoutSets() {
    }

    @Test
    @DisplayName("UT-14 Abschliessen ohne Satz wird abgewiesen")
    void finish_withoutSets_throwsConflict() {
    }

    @Test
    @DisplayName("UT-14 Abbrechen setzt eine geplante Einheit zurück")
    void revertToPlanned_removesExercisesAndStartTime() {
    }

    @Test
    @DisplayName("UT-15 Abgeschlossene Einheit kann soft gelöscht werden")
    void markDeleted_completed_setsDeleted() {
    }

    @Test
    @DisplayName("UT-15 Nur abgeschlossene Einheiten können gelöscht werden")
    void markDeleted_notCompleted_throwsException() {
    }

}
