package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.persistence.user.LoginLock;
import ch.bernet.trainingplatform.persistence.user.LoginLockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginLockServiceTest {

    private static final String EMAIL = "a@test.ch";

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
        // Arrange: 3 Fehlversuche sind bereits gezählt
        LoginLock lock = lockWithFailedAttempts(3);
        when(loginLockRepository.findByEmail(EMAIL)).thenReturn(Optional.of(lock));

        // Act: 4. Fehlversuch
        loginLockService.registerFailure(EMAIL);

        // Assert
        assertThat(lock.getFailedAttempts()).isEqualTo(4);
        assertThat(lock.getLockedUntil()).isNull();
        assertThat(loginLockService.isLocked(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("UT-04 Nach 5 Fehlversuchen ist die E-Mail 15 Minuten gesperrt")
    void registerFailure_fiveTimes_lockedFor15Minutes() {
        // Arrange: 4 Fehlversuche sind bereits gezählt
        LoginLock lock = lockWithFailedAttempts(4);
        when(loginLockRepository.findByEmail(EMAIL)).thenReturn(Optional.of(lock));

        // Act: 5. Fehlversuch
        loginLockService.registerFailure(EMAIL);

        // Assert
        assertThat(lock.getLockedUntil()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
        assertThat(loginLockService.isLocked(EMAIL)).isTrue();
    }

    @Test
    @DisplayName("UT-04 Die Sperre endet nach 15 Minuten")
    void isLocked_after15Minutes_returnsFalse() {
        // Arrange: um NOW gesperrt
        LoginLock lock = lockWithFailedAttempts(5);
        lock.setLockedUntil(NOW.plus(Duration.ofMinutes(15)));
        when(loginLockRepository.findByEmail(EMAIL)).thenReturn(Optional.of(lock));

        LoginLockService after14Minutes = new LoginLockService(loginLockRepository, Clock.offset(CLOCK, Duration.ofMinutes(14)));
        LoginLockService after15Minutes = new LoginLockService(loginLockRepository, Clock.offset(CLOCK, Duration.ofMinutes(15)));

        // Act & Assert
        assertThat(after14Minutes.isLocked(EMAIL)).isTrue();
        assertThat(after15Minutes.isLocked(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("UT-04 Erfolgreiche Anmeldung setzt die Zählung zurück")
    void clear_resetsFailedAttempts() {
        // Act
        loginLockService.clear(EMAIL);

        // Assert
        verify(loginLockRepository).deleteByEmail(EMAIL);
    }

    private static LoginLock lockWithFailedAttempts(int failedAttempts) {
        LoginLock lock = new LoginLock(EMAIL);
        lock.setFailedAttempts(failedAttempts);
        return lock;
    }

}
