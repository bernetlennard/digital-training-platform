package ch.bernet.trainingplatform.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("UT-01 Fachliche Fehler ergeben den passenden Statuscode im Fehlerformat")
    void handleDomain_returnsStatusAndMessage() {
    }

    @Test
    @DisplayName("UT-01 Unerwartete Fehler ergeben 500 ohne interne Details")
    void handleUnexpected_returns500WithoutDetails() {
    }

}
