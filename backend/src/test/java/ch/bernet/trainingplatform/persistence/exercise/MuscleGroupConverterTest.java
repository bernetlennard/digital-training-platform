package ch.bernet.trainingplatform.persistence.exercise;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MuscleGroupConverterTest {

    private final MuscleGroupConverter muscleGroupConverter = new MuscleGroupConverter();

    @Test
    @DisplayName("UT-06 Muskelgruppen werden über ihre feste ID aufgelöst")
    void fromId_knownId_returnsMuscleGroup() {
        // Die IDs entsprechen den Stammdaten in der Tabelle muscle_group (#FA-2.2)
        assertThat(MuscleGroup.fromId(1)).isEqualTo(MuscleGroup.CHEST);
        assertThat(MuscleGroup.fromId(7)).isEqualTo(MuscleGroup.LEGS);
        assertThat(MuscleGroup.fromId(8)).isEqualTo(MuscleGroup.GLUTES);
    }

    @Test
    @DisplayName("UT-06 Unbekannte Muskelgruppen-ID wird abgewiesen")
    void fromId_unknownId_throwsException() {
        assertThatThrownBy(() -> MuscleGroup.fromId(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MuscleGroup.fromId(9)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("UT-06 Muskelgruppe wird als ID gespeichert und wieder gelesen")
    void convert_roundTrip_returnsSameMuscleGroup() {
        assertThat(muscleGroupConverter.convertToDatabaseColumn(MuscleGroup.BACK)).isEqualTo(2);
        assertThat(muscleGroupConverter.convertToEntityAttribute(2)).isEqualTo(MuscleGroup.BACK);

        assertThat(muscleGroupConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(muscleGroupConverter.convertToEntityAttribute(null)).isNull();
    }

}
