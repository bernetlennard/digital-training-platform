package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.common.exception.AccountLockedException;
import ch.bernet.trainingplatform.common.exception.AuthenticationFailedException;
import ch.bernet.trainingplatform.common.exception.ConflictException;
import ch.bernet.trainingplatform.common.exception.ErrorMessages;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import ch.bernet.trainingplatform.persistence.user.AppUser;
import ch.bernet.trainingplatform.persistence.user.AppUserRepository;
import ch.bernet.trainingplatform.service.user.dto.ChangeEmailRequest;
import ch.bernet.trainingplatform.service.user.dto.ChangePasswordRequest;
import ch.bernet.trainingplatform.service.user.dto.DeleteAccountRequest;
import ch.bernet.trainingplatform.service.user.dto.LoginRequest;
import ch.bernet.trainingplatform.service.user.dto.RegisterRequest;
import ch.bernet.trainingplatform.service.user.dto.UpdateProfileRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AppUserRepository appUserRepository;
    private final LoginLockService loginLockService;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository,
                       LoginLockService loginLockService,
                       PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.loginLockService = loginLockService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // E-Mail normalisieren, Duplikat -> ConflictException, Passwort hashen, speichern
        String email = normalizeEmail(request.email());
        if (appUserRepository.existsByEmail(email)) {
            throw new ConflictException(ErrorMessages.EMAIL_ALREADY_IN_USE);
        }

        String passwordHash = passwordEncoder.encode(request.password());
        AppUser appUser = AppUser.builder()
                .email(email)
                .passwordHash(passwordHash)
                .build();

        appUserRepository.save(appUser);
        log.info("User {} registered", appUser.getId());
        return UserMapper.toResponse(appUser);
    }

    @Transactional
    public UserResponse authenticate(LoginRequest request) {
        // Sperre prüfen -> AccountLockedException, Passwort prüfen -> AuthenticationFailedException
        // bei Fehler loginLockService.registerFailure, bei Erfolg loginLockService.clear
        String email = normalizeEmail(request.email());

        if (loginLockService.isLocked(email)) {
            throw new AccountLockedException(ErrorMessages.ACCOUNT_LOCKED);
        }

        Optional<AppUser> optionalAppUser = appUserRepository.findByEmail(email);
        if (optionalAppUser.isEmpty()) {
            loginLockService.registerFailure(email);
            throw new AuthenticationFailedException(ErrorMessages.INVALID_AUTHENTICATION);
        }

        AppUser appUser = optionalAppUser.get();
        verifyPassword(appUser, request.password());
        loginLockService.clear(email);
        log.info("User {} logged in", appUser.getId());
        return UserMapper.toResponse(appUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long id) {
        // appUserRepository.findById verwenden, sonst NotFoundException
        return UserMapper.toResponse(findUser(id));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        // Vor- und Nachname setzen
        AppUser appUser = findUser(userId);
        appUser.setFirstName(request.firstName());
        appUser.setLastName(request.lastName());

        appUserRepository.save(appUser);
        log.info("Profile of user {} updated", userId);
        return UserMapper.toResponse(appUser);
    }

    @Transactional
    public UserResponse changeEmail(Long userId, ChangeEmailRequest request) {
        // Passwort bestätigen, neue E-Mail normalisieren, Duplikat -> ConflictException
        AppUser appUser = findUser(userId);
        verifyPassword(appUser, request.password());

        String normalizedMail = normalizeEmail(request.newEmail());

        if (appUserRepository.existsByEmail(normalizedMail)) {
            throw new ConflictException(ErrorMessages.EMAIL_ALREADY_IN_USE);
        }

        appUser.setEmail(normalizedMail);
        AppUser savedUser = appUserRepository.save(appUser);
        log.info("Email of user {} changed", userId);
        return UserMapper.toResponse(savedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // aktuelles Passwort bestätigen, neues Passwort hashen und speichern
        AppUser appUser = findUser(userId);
        verifyPassword(appUser, request.currentPassword());
        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        appUser.setPasswordHash(newPasswordHash);
        appUserRepository.save(appUser);
        log.info("Password of user {} changed", userId);
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        // Passwort bestätigen, Benutzer löschen (Daten werden per ON DELETE CASCADE entfernt)
        AppUser appUser = findUser(userId);
        verifyPassword(appUser, request.password());
        appUserRepository.delete(appUser);
        loginLockService.clear(appUser.getEmail());
        log.info("User {} deleted", userId);
    }

    @Transactional(readOnly = true)
    public boolean exists(Long userId) {
        // appUserRepository.existsById verwenden
        return appUserRepository.existsById(userId);
    }

    private AppUser findUser(Long userId) {
        // Benutzer laden, sonst NotFoundException
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessages.USER_NOT_FOUND));
    }

    private void verifyPassword(AppUser user, String rawPassword) {
        // passwordEncoder.matches, sonst AuthenticationFailedException
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            loginLockService.registerFailure(user.getEmail());
            throw new AuthenticationFailedException(ErrorMessages.INVALID_AUTHENTICATION);
        }
    }

    private String normalizeEmail(String email) {
        // trimmen und in Kleinbuchstaben umwandeln (ck_app_user_email)
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
