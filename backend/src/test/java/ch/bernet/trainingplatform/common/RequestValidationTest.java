package ch.bernet.trainingplatform.common;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestValidationTest {

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
    }

    @Test
    @DisplayName("UT-03 Passwort mit weniger als 15 oder mehr als 64 Zeichen ist ungültig")
    void registerRequest_passwordOutOfLimits_isInvalid() {
    }

    @Test
    @DisplayName("UT-03 Leere, ungültige oder zu lange E-Mail ist ungültig")
    void registerRequest_invalidEmail_isInvalid() {
    }

    @Test
    @DisplayName("UT-05 Vor- oder Nachname mit mehr als 100 Zeichen ist ungültig")
    void updateProfileRequest_nameTooLong_isInvalid() {
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

}
