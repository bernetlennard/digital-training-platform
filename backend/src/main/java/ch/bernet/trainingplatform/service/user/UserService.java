package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.common.AuthenticationFailedException;
import ch.bernet.trainingplatform.persistence.user.AppUser;
import ch.bernet.trainingplatform.persistence.user.AppUserRepository;
import ch.bernet.trainingplatform.service.user.dto.ChangeEmailRequest;
import ch.bernet.trainingplatform.service.user.dto.ChangePasswordRequest;
import ch.bernet.trainingplatform.service.user.dto.DeleteAccountRequest;
import ch.bernet.trainingplatform.service.user.dto.LoginRequest;
import ch.bernet.trainingplatform.service.user.dto.RegisterRequest;
import ch.bernet.trainingplatform.service.user.dto.UpdateProfileRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

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
        String email = normalizeEmail(request.email());
        String passwordHash = passwordEncoder.encode(request.password());
        AppUser appUser = AppUser.builder()
                .email(email)
                .passwordHash(passwordHash)
                .build();

        AppUser savedUser = appUserRepository.save(appUser);
        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName());
    }

    @Transactional
    public UserResponse authenticate(LoginRequest request) {
        // TODO: Sperre prüfen -> AccountLockedException (#NFA-1.3), Passwort prüfen -> AuthenticationFailedException (#NFA-1.4)
        // TODO: bei Fehler loginLockService.registerFailure, bei Erfolg loginLockService.clear

        String password = request.password();
        AppUser appUser = appUserRepository.findByEmail(normalizeEmail(request.email())).orElseThrow();
        verifyPassword(appUser, password);

        return new UserResponse(appUser.getId(), appUser.getEmail(), appUser.getFirstName(), appUser.getLastName());
    }

    // TODO: an Diagramm angleichen (findUser verwenden, NotFoundException statt null, UserMapper.toResponse)
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long id) {
        AppUser appUser = appUserRepository.findById(id).orElse(null);
        if (appUser == null) {
            return null;
        }
        return new UserResponse(appUser.getId(), appUser.getEmail(), appUser.getFirstName(), appUser.getLastName());
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        // TODO: Vor- und Nachname setzen
        return null;
    }

    @Transactional
    public UserResponse changeEmail(Long userId, ChangeEmailRequest request) {
        // TODO: Passwort bestätigen, neue E-Mail normalisieren, Duplikat -> ConflictException
        return null;
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // TODO: aktuelles Passwort bestätigen, neues Passwort hashen und speichern
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        // TODO: Passwort bestätigen, Benutzer löschen (Daten werden per ON DELETE CASCADE entfernt)
    }

    @Transactional(readOnly = true)
    public boolean exists(Long userId) {
        return appUserRepository.existsById(userId);
    }

    private AppUser findUser(Long userId) {
        // TODO: Benutzer laden, sonst NotFoundException
        return null;
    }

    private void verifyPassword(AppUser user, String rawPassword) {
        if(!passwordEncoder.matches(rawPassword, user.getPasswordHash())){
            throw new AuthenticationFailedException("Invalid password.");
        }
        // TODO: passwordEncoder.matches, sonst AuthenticationFailedException
    }

    private String normalizeEmail(String email) {
        // TODO: trimmen und in Kleinbuchstaben umwandeln (ck_app_user_email)
        return email.trim().toLowerCase();
    }

}
