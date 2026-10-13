package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.common.AccountLockedException;
import ch.bernet.trainingplatform.common.AuthenticationFailedException;
import ch.bernet.trainingplatform.common.ConflictException;
import ch.bernet.trainingplatform.persistence.user.AppUser;
import ch.bernet.trainingplatform.persistence.user.AppUserRepository;
import ch.bernet.trainingplatform.service.user.dto.ChangeEmailRequest;
import ch.bernet.trainingplatform.service.user.dto.ChangePasswordRequest;
import ch.bernet.trainingplatform.service.user.dto.DeleteAccountRequest;
import ch.bernet.trainingplatform.service.user.dto.LoginRequest;
import ch.bernet.trainingplatform.service.user.dto.RegisterRequest;
import ch.bernet.trainingplatform.service.user.dto.UpdateProfileRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static ch.bernet.trainingplatform.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "a@test.ch";
    private static final String PASSWORD = "ein-langes-passwort";
    private static final String WRONG_PASSWORD = "ein-falsches-passwort";
    private static final String PASSWORD_HASH = "hash";

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private LoginLockService loginLockService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("UT-03 Registrieren speichert die normalisierte E-Mail und den Hash")
    void register_valid_storesNormalizedEmailAndHash() {
        // Arrange
        RegisterRequest request = new RegisterRequest("  A@Test.CH ", "ein-langes-passwort");
        when(passwordEncoder.encode("ein-langes-passwort")).thenReturn("hash");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse response = userService.register(request);

        // Assert
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        AppUser savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("a@test.ch");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hash");
        assertThat(response.email()).isEqualTo("a@test.ch");
    }

    @Test
    @DisplayName("UT-03 Vorhandene E-Mail ergibt 409")
    void register_existingEmail_throwsConflict() {
        // Arrange: Gross-/Kleinschreibung wird nicht unterschieden (#FA-1.3)
        when(appUserRepository.existsByEmail(EMAIL)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.register(new RegisterRequest("  A@Test.CH ", PASSWORD)))
                .isInstanceOf(ConflictException.class);

        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-03 Anmelden mit korrekten Daten setzt die Fehlversuche zurück")
    void authenticate_valid_clearsFailedAttempts() {
        // Arrange
        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

        // Act
        UserResponse response = userService.authenticate(new LoginRequest("  A@Test.CH ", PASSWORD));

        // Assert
        assertThat(response).isEqualTo(new UserResponse(USER_A, EMAIL, "Anna", "Muster"));
        verify(loginLockService).isLocked(EMAIL);
        verify(loginLockService).clear(EMAIL);
        verify(loginLockService, never()).registerFailure(any());
    }

    @Test
    @DisplayName("UT-04 Falsches Passwort ergibt 401 und zählt den Fehlversuch")
    void authenticate_wrongPassword_throwsAndRegistersFailure() {
        // Arrange
        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.authenticate(new LoginRequest(EMAIL, WRONG_PASSWORD)))
                .isInstanceOf(AuthenticationFailedException.class);

        verify(loginLockService).registerFailure(EMAIL);
        verify(loginLockService, never()).clear(any());
    }

    @Test
    @DisplayName("UT-04 Unbekannte E-Mail ergibt dieselbe Antwort wie ein falsches Passwort")
    void authenticate_unknownEmail_throwsSameException() {
        // Arrange
        when(appUserRepository.findByEmail("unbekannt@test.ch")).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));

        // Act
        Throwable unknownEmail = catchThrowable(() -> userService.authenticate(new LoginRequest("unbekannt@test.ch", PASSWORD)));
        Throwable wrongPassword = catchThrowable(() -> userService.authenticate(new LoginRequest(EMAIL, WRONG_PASSWORD)));

        // Assert
        assertThat(wrongPassword).isInstanceOf(AuthenticationFailedException.class);
        assertThat(unknownEmail).isInstanceOf(AuthenticationFailedException.class)
                .hasMessage(wrongPassword.getMessage());

        // Der Fehlversuch zählt auch für E-Mails ohne Konto (#NFA-1.3)
        verify(loginLockService).registerFailure("unbekannt@test.ch");
    }

    @Test
    @DisplayName("UT-04 Gesperrte E-Mail ergibt 429 ohne Passwortprüfung")
    void authenticate_locked_throwsAccountLocked() {
        // Arrange
        when(loginLockService.isLocked(EMAIL)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.authenticate(new LoginRequest(EMAIL, PASSWORD)))
                .isInstanceOf(AccountLockedException.class);

        verifyNoInteractions(passwordEncoder);
        verify(loginLockService, never()).registerFailure(any());
    }

    @Test
    @DisplayName("UT-05 Profil ändern speichert Vor- und Nachname")
    void updateProfile_savesNames() {
        // Arrange
        AppUser user = user();
        when(appUserRepository.findById(USER_A)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.updateProfile(USER_A, new UpdateProfileRequest("Lena", "Beispiel"));

        // Assert
        assertThat(user.getFirstName()).isEqualTo("Lena");
        assertThat(user.getLastName()).isEqualTo("Beispiel");
        assertThat(response).isEqualTo(new UserResponse(USER_A, EMAIL, "Lena", "Beispiel"));
    }

    @Test
    @DisplayName("UT-05 E-Mail ändern mit falschem Passwort ergibt 401")
    void changeEmail_wrongPassword_throwsAuthenticationFailed() {
        // Arrange
        AppUser user = user();
        when(appUserRepository.findById(USER_A)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.changeEmail(USER_A, new ChangeEmailRequest("neu@test.ch", WRONG_PASSWORD)))
                .isInstanceOf(AuthenticationFailedException.class);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-05 E-Mail ändern mit korrektem Passwort speichert die neue E-Mail")
    void changeEmail_correctPassword_savesNewEmail() {
        // Arrange: jeder findById-Aufruf liefert eine frische Kopie, wie ein neuer DB-Zugriff
        when(appUserRepository.findById(USER_A)).thenAnswer(invocation -> Optional.of(user()));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse response = userService.changeEmail(USER_A, new ChangeEmailRequest("  Neu@Test.CH ", PASSWORD));

        // Assert
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("neu@test.ch");
        assertThat(response).isEqualTo(new UserResponse(USER_A, "neu@test.ch", "Anna", "Muster"));
    }


    @Test
    @DisplayName("UT-05 E-Mail ändern auf eine vergebene Adresse ergibt 409")
    void changeEmail_existingEmail_throwsConflict() {
        // Arrange
        AppUser user = user();
        when(appUserRepository.findById(USER_A)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(appUserRepository.existsByEmail("b@test.ch")).thenReturn(true);

        // Act & Assert: die neue Adresse wird vor der Prüfung normalisiert
        assertThatThrownBy(() -> userService.changeEmail(USER_A, new ChangeEmailRequest("  B@Test.CH ", PASSWORD)))
                .isInstanceOf(ConflictException.class);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("UT-05 Passwort ändern speichert den neuen Hash")
    void changePassword_valid_storesNewHash() {
        // Arrange
        AppUser user = user();
        when(appUserRepository.findById(USER_A)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(passwordEncoder.encode("ein-neues-langes-passwort")).thenReturn("neuer-hash");

        // Act
        userService.changePassword(USER_A, new ChangePasswordRequest(PASSWORD, "ein-neues-langes-passwort"));

        // Assert
        assertThat(user.getPasswordHash()).isEqualTo("neuer-hash");
    }

    @Test
    @DisplayName("UT-05 Konto löschen entfernt Benutzer und Sperreintrag")
    void deleteAccount_valid_deletesUserAndLock() throws Exception {
        // Arrange
        AppUser user = user();
        when(appUserRepository.findById(USER_A)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

        // Act
        userService.deleteAccount(USER_A, new DeleteAccountRequest(PASSWORD));

        // Assert
        verify(appUserRepository).delete(user);
        verify(loginLockService).clear(EMAIL);

        // Beide Löschungen laufen in einer Transaktion (#NFA-1.6)
        assertThat(UserService.class.getMethod("deleteAccount", Long.class, DeleteAccountRequest.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    @DisplayName("UT-05 Konto löschen mit falschem Passwort löscht nichts")
    void deleteAccount_wrongPassword_deletesNothing() {
        // Arrange
        when(appUserRepository.findById(USER_A)).thenReturn(Optional.of(user()));
        when(passwordEncoder.matches(WRONG_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteAccount(USER_A, new DeleteAccountRequest(WRONG_PASSWORD)))
                .isInstanceOf(AuthenticationFailedException.class);

        verify(appUserRepository, never()).delete(any());
        verify(appUserRepository, never()).deleteById(any());
        verify(loginLockService).registerFailure(EMAIL);
    }

    private static AppUser user() {
        return AppUser.builder()
                .id(USER_A)
                .email(EMAIL)
                .passwordHash(PASSWORD_HASH)
                .firstName("Anna")
                .lastName("Muster")
                .build();
    }
}
