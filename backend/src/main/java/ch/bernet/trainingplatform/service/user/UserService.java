package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.common.AccountLockedException;
import ch.bernet.trainingplatform.common.AuthenticationFailedException;
import ch.bernet.trainingplatform.common.ConflictException;
import ch.bernet.trainingplatform.common.NotFoundException;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        if (appUserRepository.existsByEmail(email)) {
            throw new ConflictException("Email address already in use.");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        AppUser appUser = AppUser.builder()
                .email(email)
                .passwordHash(passwordHash)
                .build();

        appUserRepository.save(appUser);
        return UserMapper.toResponse(appUser);
    }

    @Transactional
    public UserResponse authenticate(LoginRequest request) {
        String email = normalizeEmail(request.email());

        if (loginLockService.isLocked(email)) {
            throw new AccountLockedException("Account locked.");
        }

        Optional<AppUser> optionalAppUser = appUserRepository.findByEmail(email);
        if (optionalAppUser.isEmpty()) {
            loginLockService.registerFailure(email);
            throw new AuthenticationFailedException("Invalid Authentication.");
        }

        AppUser appUser = optionalAppUser.get();
        String password = request.password();
        verifyPassword(appUser, password);

        loginLockService.clear(email);

        return UserMapper.toResponse(appUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long id) {
        AppUser appUser = returnAppUserIfExists(id);
        return UserMapper.toResponse(appUser);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        AppUser appUser = returnAppUserIfExists(userId);
        appUser.setFirstName(request.firstName());
        appUser.setLastName(request.lastName());

        appUserRepository.save(appUser);
        return UserMapper.toResponse(appUser);
    }

    @Transactional
    public UserResponse changeEmail(Long userId, ChangeEmailRequest request) {
        AppUser appUser = returnAppUserIfExists(userId);
        verifyPassword(appUser, request.password());

        String normalizedMail = normalizeEmail(request.newEmail());

        if (appUserRepository.existsByEmail(normalizedMail)) {
            throw new ConflictException("Email address already in use.");
        }

        appUser.setEmail(normalizedMail);
        AppUser savedUser = appUserRepository.save(appUser);
        return UserMapper.toResponse(savedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        AppUser appUser = returnAppUserIfExists(userId);
        verifyPassword(appUser, request.currentPassword());
        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        appUser.setPasswordHash(newPasswordHash);
        appUserRepository.save(appUser);
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        AppUser appUser = returnAppUserIfExists(userId);
        verifyPassword(appUser, request.password());
        appUserRepository.delete(appUser);
        loginLockService.clear(appUser.getEmail());
    }

    @Transactional(readOnly = true)
    public boolean exists(Long userId) {
        return appUserRepository.existsById(userId);
    }

    private AppUser returnAppUserIfExists(Long userId) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(userId);
        if (optionalAppUser.isEmpty()) {
            throw new NotFoundException("User not found.");
        }
        return optionalAppUser.get();
    }

    private void verifyPassword(AppUser user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            loginLockService.registerFailure(user.getEmail());
            throw new AuthenticationFailedException("Invalid Authentication.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

}
