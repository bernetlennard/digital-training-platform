package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.persistence.user.LoginLockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static ch.bernet.trainingplatform.TestData.*;

@ExtendWith(MockitoExtension.class)
class LoginLockServiceTest {

    @Mock
    private LoginLockRepository loginLockRepository;

    private LoginLockService loginLockService;

    @BeforeEach
    void setUp() {
        loginLockService = new LoginLockService(loginLockRepository, CLOCK);
    }

    @Test
    @DisplayName("UT-04 Nach 4 Fehlversuchen ist die E-Mail nicht gesperrt")
    void registerFailure_fourTimes_notLocked() {
    }

    @Test
    @DisplayName("UT-04 Nach 5 Fehlversuchen ist die E-Mail 15 Minuten gesperrt")
    void registerFailure_fiveTimes_lockedFor15Minutes() {
    }

    @Test
    @DisplayName("UT-04 Die Sperre endet nach 15 Minuten")
    void isLocked_after15Minutes_returnsFalse() {
    }

    @Test
    @DisplayName("UT-04 Erfolgreiche Anmeldung setzt die Zählung zurück")
    void clear_resetsFailedAttempts() {
    }

}
