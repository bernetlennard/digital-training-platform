package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.persistence.user.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static ch.bernet.trainingplatform.TestData.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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
    }

    @Test
    @DisplayName("UT-03 Vorhandene E-Mail ergibt 409")
    void register_existingEmail_throwsConflict() {
    }

    @Test
    @DisplayName("UT-03 Anmelden mit korrekten Daten setzt die Fehlversuche zurück")
    void authenticate_valid_clearsFailedAttempts() {
    }

    @Test
    @DisplayName("UT-04 Falsches Passwort ergibt 401 und zählt den Fehlversuch")
    void authenticate_wrongPassword_throwsAndRegistersFailure() {
    }

    @Test
    @DisplayName("UT-04 Unbekannte E-Mail ergibt dieselbe Antwort wie ein falsches Passwort")
    void authenticate_unknownEmail_throwsSameException() {
    }

    @Test
    @DisplayName("UT-04 Gesperrte E-Mail ergibt 429 ohne Passwortprüfung")
    void authenticate_locked_throwsAccountLocked() {
    }

    @Test
    @DisplayName("UT-05 Profil ändern speichert Vor- und Nachname")
    void updateProfile_savesNames() {
    }

    @Test
    @DisplayName("UT-05 E-Mail ändern mit falschem Passwort ergibt 401")
    void changeEmail_wrongPassword_throwsAuthenticationFailed() {
    }

    @Test
    @DisplayName("UT-05 E-Mail ändern auf eine vergebene Adresse ergibt 409")
    void changeEmail_existingEmail_throwsConflict() {
    }

    @Test
    @DisplayName("UT-05 Passwort ändern speichert den neuen Hash")
    void changePassword_valid_storesNewHash() {
    }

    @Test
    @DisplayName("UT-05 Konto löschen entfernt Benutzer und Sperreintrag")
    void deleteAccount_valid_deletesUserAndLock() {
    }

    @Test
    @DisplayName("UT-05 Konto löschen mit falschem Passwort löscht nichts")
    void deleteAccount_wrongPassword_deletesNothing() {
    }

}
