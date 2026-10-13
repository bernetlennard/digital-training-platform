package ch.bernet.trainingplatform.common;

import ch.bernet.trainingplatform.service.user.dto.RegisterRequest;
import ch.bernet.trainingplatform.service.user.dto.UpdateProfileRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    private static final String VALID_EMAIL = "a@test.ch";
    private static final String VALID_PASSWORD = "ein-langes-passwort";

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("UT-03 Passwort mit 15 bis 64 Zeichen ist gültig")
    void registerRequest_passwordWithinLimits_isValid() {
        assertThat(violatedFields(new RegisterRequest(VALID_EMAIL, "a".repeat(15)))).isEmpty();
        assertThat(violatedFields(new RegisterRequest(VALID_EMAIL, "a".repeat(64)))).isEmpty();
    }

    @Test
    @DisplayName("UT-03 Passwort mit weniger als 15 oder mehr als 64 Zeichen ist ungültig")
    void registerRequest_passwordOutOfLimits_isInvalid() {
        assertThat(violatedFields(new RegisterRequest(VALID_EMAIL, null))).containsOnly("password");
        assertThat(violatedFields(new RegisterRequest(VALID_EMAIL, "a".repeat(14)))).containsOnly("password");
        assertThat(violatedFields(new RegisterRequest(VALID_EMAIL, "a".repeat(65)))).containsOnly("password");
    }

    @Test
    @DisplayName("UT-03 Leere, ungültige oder zu lange E-Mail ist ungültig")
    void registerRequest_invalidEmail_isInvalid() {
        // Gültiges Format, aber 255 Zeichen lang
        String tooLongEmail = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(59) + ".ch";
        assertThat(tooLongEmail).hasSize(255);

        assertThat(violatedFields(new RegisterRequest(null, VALID_PASSWORD))).containsOnly("email");
        assertThat(violatedFields(new RegisterRequest("", VALID_PASSWORD))).containsOnly("email");
        assertThat(violatedFields(new RegisterRequest("keine-email", VALID_PASSWORD))).containsOnly("email");
        assertThat(violatedFields(new RegisterRequest(tooLongEmail, VALID_PASSWORD))).containsOnly("email");
    }

    @Test
    @DisplayName("UT-05 Vor- oder Nachname mit mehr als 100 Zeichen ist ungültig")
    void updateProfileRequest_nameTooLong_isInvalid() {
        assertThat(violatedFields(new UpdateProfileRequest("a".repeat(101), "Muster"))).containsOnly("firstName");
        assertThat(violatedFields(new UpdateProfileRequest("Anna", "a".repeat(101)))).containsOnly("lastName");

        // Beide Felder sind optional, 100 Zeichen sind erlaubt
        assertThat(violatedFields(new UpdateProfileRequest("a".repeat(100), null))).isEmpty();
    }

    @Test
    @DisplayName("UT-06 Übung ohne Name oder mit zu langen Feldern ist ungültig")
    void exerciseRequest_missingOrTooLongFields_isInvalid() {
    }

    @Test
    @DisplayName("UT-06 Unbekannte Muskelgruppe ist ungültig")
    void exerciseRequest_unknownMuscleGroup_isInvalid() {
    }

    @Test
    @DisplayName("UT-08 Routine ohne Name oder ohne Übung ist ungültig")
    void routineRequest_missingNameOrExercises_isInvalid() {
    }

    @Test
    @DisplayName("UT-10 Planung ohne Routine oder Datum ist ungültig")
    void planSessionRequest_missingFields_isInvalid() {
    }

    @Test
    @DisplayName("UT-12 Satzwerte innerhalb der Grenzen sind gültig")
    void setRequest_valuesWithinLimits_isValid() {
    }

    @Test
    @DisplayName("UT-12 Satzwerte ausserhalb der Grenzen oder mit mehr als 2 Nachkommastellen sind ungültig")
    void setRequest_valuesOutOfLimits_isInvalid() {
    }

    @Test
    @DisplayName("UT-12 Verschieben auf Satznummer 0 ist ungültig")
    void moveSetRequest_zero_isInvalid() {
    }

    private static Set<String> violatedFields(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

}
