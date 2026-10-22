package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.persistence.user.LoginLock;
import ch.bernet.trainingplatform.persistence.user.LoginLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

@Service
public class LoginLockService {

    // Logs enthalten nur die ID des Eintrags, nie die E-Mail-Adresse
    private static final Logger log = LoggerFactory.getLogger(LoginLockService.class);
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
        // true, wenn lockedUntil in der Zukunft liegt, der abgewiesene Versuch wird geloggt
        Optional<LoginLock> activeLock = loginLockRepository.findByEmail(email)
                .filter(loginLock -> loginLock.getLockedUntil() != null
                        && loginLock.getLockedUntil().isAfter(clock.instant()));
        activeLock.ifPresent(loginLock -> log.warn("Login rejected while locked, login lock {}", loginLock.getId()));
        return activeLock.isPresent();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailure(String email) {
        // Fehlversuche hochzählen und loggen, ab MAX_FAILED_ATTEMPTS für LOCK_DURATION sperren
        Optional<LoginLock> optionalLoginLock = loginLockRepository.findByEmail(email);
        if (optionalLoginLock.isEmpty()) {
            LoginLock loginLock = LoginLock.builder().email(email).failedAttempts(1).build();
            loginLockRepository.save(loginLock);
            log.warn("Failed authentication attempt 1, login lock {}", loginLock.getId());
            return;
        }
        LoginLock loginLock = optionalLoginLock.get();
        int failedAttempts = loginLock.getFailedAttempts() + 1;
        loginLock.setFailedAttempts(failedAttempts);
        log.warn("Failed authentication attempt {}, login lock {}", failedAttempts, loginLock.getId());
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            loginLock.setLockedUntil(clock.instant().plus(LOCK_DURATION));
            log.warn("Login lock {} locked until {}", loginLock.getId(), loginLock.getLockedUntil());
        }
        loginLockRepository.save(loginLock);
    }

    @Transactional
    public void clear(String email) {
        // Eintrag nach erfolgreichem Login löschen
        loginLockRepository.deleteByEmail(email);
    }

}
