package ch.bernet.trainingplatform.common;

import ch.bernet.trainingplatform.service.exercise.dto.ExerciseRequest;
import ch.bernet.trainingplatform.service.routine.dto.RoutineRequest;
import ch.bernet.trainingplatform.service.training.dto.MoveSetRequest;
import ch.bernet.trainingplatform.service.training.dto.NoteRequest;
import ch.bernet.trainingplatform.service.training.dto.PlanSessionRequest;
import ch.bernet.trainingplatform.service.training.dto.SetRequest;
import ch.bernet.trainingplatform.service.training.dto.StartSessionRequest;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.bernet.trainingplatform.TestData.*;
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
    @DisplayName("UT-06 Übung mit Name, Muskelgruppe und optionaler Beschreibung ist gültig")
    void exerciseRequest_valid_isValid() {
        assertThat(violatedFields(new ExerciseRequest("Bankdrücken", 1, null))).isEmpty();
        assertThat(violatedFields(new ExerciseRequest("a".repeat(100), 8, "a".repeat(1000)))).isEmpty();
    }

    @Test
    @DisplayName("UT-06 Übung ohne Name oder mit zu langen Feldern ist ungültig")
    void exerciseRequest_missingOrTooLongFields_isInvalid() {
        assertThat(violatedFields(new ExerciseRequest(null, 1, null))).containsOnly("name");
        assertThat(violatedFields(new ExerciseRequest(" ", 1, null))).containsOnly("name");
        assertThat(violatedFields(new ExerciseRequest("a".repeat(101), 1, null))).containsOnly("name");
        assertThat(violatedFields(new ExerciseRequest("Bankdrücken", 1, "a".repeat(1001)))).containsOnly("description");
    }

    @Test
    @DisplayName("UT-06 Unbekannte Muskelgruppe ist ungültig")
    void exerciseRequest_unknownMuscleGroup_isInvalid() {
        // 0 ist auch der Wert des primitiven Typs, wenn das Feld fehlt
        assertThat(violatedFields(new ExerciseRequest("Bankdrücken", 0, null))).containsOnly("muscleGroupId");
        assertThat(violatedFields(new ExerciseRequest("Bankdrücken", 9, null))).containsOnly("muscleGroupId");
    }

    @Test
    @DisplayName("UT-08 Routine mit Name und mindestens einer Übung ist gültig")
    void routineRequest_valid_isValid() {
        assertThat(violatedFields(new RoutineRequest("Push", List.of(BENCH_PRESS)))).isEmpty();
        assertThat(violatedFields(new RoutineRequest("a".repeat(100), List.of(BENCH_PRESS, SQUAT)))).isEmpty();
    }

    @Test
    @DisplayName("UT-08 Routine ohne Name oder ohne Übung ist ungültig")
    void routineRequest_missingNameOrExercises_isInvalid() {
        assertThat(violatedFields(new RoutineRequest(null, List.of(BENCH_PRESS)))).containsOnly("name");
        assertThat(violatedFields(new RoutineRequest(" ", List.of(BENCH_PRESS)))).containsOnly("name");
        assertThat(violatedFields(new RoutineRequest("a".repeat(101), List.of(BENCH_PRESS)))).containsOnly("name");
        assertThat(violatedFields(new RoutineRequest("Push", null))).containsOnly("exerciseIds");
        assertThat(violatedFields(new RoutineRequest("Push", List.of()))).containsOnly("exerciseIds");
    }

    @Test
    @DisplayName("UT-08 Leere Übungs-ID in der Liste ist ungültig")
    void routineRequest_nullExerciseId_isInvalid() {
        // Ohne diese Regel würde eine leere ID erst im Service zu einem 500 führen
        assertThat(violatedFields(new RoutineRequest("Push", Arrays.asList(BENCH_PRESS, null))))
                .singleElement().asString().startsWith("exerciseIds");
    }

    @Test
    @DisplayName("UT-10 Planung ohne Routine oder Datum ist ungültig")
    void planSessionRequest_missingFields_isInvalid() {
        assertThat(violatedFields(new PlanSessionRequest(ROUTINE_ID, TODAY))).isEmpty();
        assertThat(violatedFields(new PlanSessionRequest(null, TODAY))).containsOnly("routineId");
        assertThat(violatedFields(new PlanSessionRequest(ROUTINE_ID, null))).containsOnly("plannedDate");
    }

    @Test
    @DisplayName("UT-11 Spontaner Start ohne Routine ist ungültig")
    void startSessionRequest_missingRoutine_isInvalid() {
        assertThat(violatedFields(new StartSessionRequest(ROUTINE_ID))).isEmpty();
        assertThat(violatedFields(new StartSessionRequest(null))).containsOnly("routineId");
    }

    @Test
    @DisplayName("UT-12 Satzwerte innerhalb der Grenzen sind gültig")
    void setRequest_valuesWithinLimits_isValid() {
        assertThat(violatedFields(new SetRequest(new BigDecimal("0"), 1, null))).isEmpty();
        assertThat(violatedFields(new SetRequest(new BigDecimal("1000.00"), 100, 3600))).isEmpty();
        assertThat(violatedFields(new SetRequest(new BigDecimal("82.25"), 8, 0))).isEmpty();
    }

    @Test
    @DisplayName("UT-12 Satzwerte ausserhalb der Grenzen oder mit mehr als 2 Nachkommastellen sind ungültig")
    void setRequest_valuesOutOfLimits_isInvalid() {
        assertThat(violatedFields(new SetRequest(null, 8, 90))).containsOnly("weightKg");
        assertThat(violatedFields(new SetRequest(new BigDecimal("-0.01"), 8, 90))).containsOnly("weightKg");
        assertThat(violatedFields(new SetRequest(new BigDecimal("1000.01"), 8, 90))).containsOnly("weightKg");
        assertThat(violatedFields(new SetRequest(new BigDecimal("82.125"), 8, 90))).containsOnly("weightKg");

        // 0 ist auch der Wert des primitiven Typs, wenn das Feld fehlt
        assertThat(violatedFields(new SetRequest(new BigDecimal("80"), 0, 90))).containsOnly("repetitions");
        assertThat(violatedFields(new SetRequest(new BigDecimal("80"), 101, 90))).containsOnly("repetitions");

        assertThat(violatedFields(new SetRequest(new BigDecimal("80"), 8, -1))).containsOnly("restSeconds");
        assertThat(violatedFields(new SetRequest(new BigDecimal("80"), 8, 3601))).containsOnly("restSeconds");
    }

    @Test
    @DisplayName("UT-12 Verschieben auf Satznummer 0 ist ungültig")
    void moveSetRequest_zero_isInvalid() {
        assertThat(violatedFields(new MoveSetRequest(1))).isEmpty();
        assertThat(violatedFields(new MoveSetRequest(0))).containsOnly("newSetNumber");
    }

    @Test
    @DisplayName("UT-01 Notiz mit mehr als 1000 Zeichen ist ungültig")
    void noteRequest_tooLong_isInvalid() {
        assertThat(violatedFields(new NoteRequest(null))).isEmpty();
        assertThat(violatedFields(new NoteRequest("a".repeat(1000)))).isEmpty();
        assertThat(violatedFields(new NoteRequest("a".repeat(1001)))).containsOnly("note");
    }

    private static Set<String> violatedFields(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

}
