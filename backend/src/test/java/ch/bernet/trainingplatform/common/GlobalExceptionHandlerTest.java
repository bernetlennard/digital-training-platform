package ch.bernet.trainingplatform.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("UT-01 Fachliche Fehler ergeben den passenden Statuscode im Fehlerformat")
    void handleDomain_returnsStatusAndMessage() {
        assertErrorResponse(new AuthenticationFailedException(ErrorMessages.INVALID_AUTHENTICATION), HttpStatus.UNAUTHORIZED);
        assertErrorResponse(new NotFoundException(ErrorMessages.ROUTINE_NOT_FOUND), HttpStatus.NOT_FOUND);
        assertErrorResponse(new ConflictException(ErrorMessages.NAME_ALREADY_IN_USE), HttpStatus.CONFLICT);
        assertErrorResponse(new BusinessRuleException(ErrorMessages.PLANNED_DATE_IN_PAST), HttpStatus.UNPROCESSABLE_CONTENT);
        assertErrorResponse(new AccountLockedException(ErrorMessages.ACCOUNT_LOCKED), HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("UT-01 Unerwartete Fehler ergeben 500 ohne interne Details")
    void handleUnexpected_returns500WithoutDetails() {
        // Arrange
        IllegalStateException exception = new IllegalStateException("ERROR: relation app_user violates ck_app_user_email");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnexpected(exception);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.message()).isNotBlank()
                .doesNotContain("app_user", "ck_app_user_email", "IllegalStateException");
        assertThat(body.details()).isEmpty();
    }

    private void assertErrorResponse(DomainException exception, HttpStatus expectedStatus) {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDomain(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody())
                .isEqualTo(new ErrorResponse(expectedStatus.value(), exception.getMessage(), List.of()));
    }

}
