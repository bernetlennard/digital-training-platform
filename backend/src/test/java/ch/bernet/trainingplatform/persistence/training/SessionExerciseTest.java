package ch.bernet.trainingplatform.persistence.training;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ch.bernet.trainingplatform.TestData.*;

class SessionExerciseTest {

    @Test
    @DisplayName("UT-12 Sätze werden fortlaufend nummeriert")
    void addSet_numbersSetsSequentially() {
    }

    @Test
    @DisplayName("UT-12 Satz ändern behält die Satznummer")
    void updateSet_replacesValuesAndKeepsNumber() {
    }

    @Test
    @DisplayName("UT-12 Nach dem Löschen sind die Satznummern lückenlos")
    void removeSet_renumbersWithoutGaps() {
    }

    @Test
    @DisplayName("UT-12 Verschieben ordnet die Sätze neu und nummeriert lückenlos")
    void moveSet_reordersAndRenumbers() {
    }

    @Test
    @DisplayName("UT-12 Verschieben ausserhalb der vorhandenen Sätze ergibt 422")
    void moveSet_outOfRange_throwsBusinessRule() {
    }

    @Test
    @DisplayName("UT-12 Unbekannter Satz ergibt 404")
    void updateOrRemoveSet_unknownId_throwsNotFound() {
    }

}
