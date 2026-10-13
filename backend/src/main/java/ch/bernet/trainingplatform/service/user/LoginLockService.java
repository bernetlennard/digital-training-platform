package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.persistence.user.LoginLock;
import ch.bernet.trainingplatform.persistence.user.LoginLockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class LoginLockService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final LoginLockRepository loginLockRepository;
    private final Clock clock;

    public LoginLockService(LoginLockRepository loginLockRepository, Clock clock) {
        this.loginLockRepository = loginLockRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public boolean isLocked(String email) {
        // TODO: true, wenn lockedUntil in der Zukunft liegt
        Optional<LoginLock> optionalLoginLock = loginLockRepository.findByEmail(email);
        if (optionalLoginLock.isEmpty()) {
            return false;
        }

        LoginLock loginLock = optionalLoginLock.get();
        Instant lockedUntil = loginLock.getLockedUntil();
        if (lockedUntil != null) {
            return lockedUntil.isAfter(clock.instant());
        }
        return false;
    }

    @Transactional
    public void registerFailure(String email) {
        // TODO: Fehlversuche hochzählen, ab MAX_FAILED_ATTEMPTS für LOCK_DURATION sperren (#NFA-1.3)
        Optional<LoginLock> optionalLoginLock = loginLockRepository.findByEmail(email);
        if (optionalLoginLock.isEmpty()) {
            loginLockRepository.save(LoginLock.builder().email(email).failedAttempts(1).build());
            return;
        }
        LoginLock loginLock = optionalLoginLock.get();
        int failedAttempts = loginLock.getFailedAttempts() + 1;
        loginLock.setFailedAttempts(failedAttempts);
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            loginLock.setLockedUntil(clock.instant().plus(LOCK_DURATION));
        }
        loginLockRepository.save(loginLock);
    }

    @Transactional
    public void clear(String email) {
        // TODO: Eintrag nach erfolgreichem Login löschen
        loginLockRepository.deleteByEmail(email);
    }

}
