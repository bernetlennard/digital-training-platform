package ch.bernet.trainingplatform.persistence.training;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionStatusConverterTest {

    private final SessionStatusConverter sessionStatusConverter = new SessionStatusConverter();

    @Test
    @DisplayName("UT-11 Status werden über ihre feste ID aufgelöst")
    void fromId_knownId_returnsStatus() {
        // Die IDs entsprechen den Stammdaten in der Tabelle session_status
        assertThat(SessionStatus.fromId(1)).isEqualTo(SessionStatus.PLANNED);
        assertThat(SessionStatus.fromId(2)).isEqualTo(SessionStatus.ACTIVE);
        assertThat(SessionStatus.fromId(3)).isEqualTo(SessionStatus.COMPLETED);
    }

    @Test
    @DisplayName("UT-11 Unbekannte Status-ID wird abgewiesen")
    void fromId_unknownId_throwsException() {
        assertThatThrownBy(() -> SessionStatus.fromId(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SessionStatus.fromId(4)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("UT-11 Status wird als ID gespeichert und wieder gelesen")
    void convert_roundTrip_returnsSameStatus() {
        assertThat(sessionStatusConverter.convertToDatabaseColumn(SessionStatus.ACTIVE)).isEqualTo(2);
        assertThat(sessionStatusConverter.convertToEntityAttribute(2)).isEqualTo(SessionStatus.ACTIVE);

        assertThat(sessionStatusConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(sessionStatusConverter.convertToEntityAttribute(null)).isNull();
    }

}
