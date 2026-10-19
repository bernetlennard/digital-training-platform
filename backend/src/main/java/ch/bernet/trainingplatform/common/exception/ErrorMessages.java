package ch.bernet.trainingplatform.common.exception;

public final class ErrorMessages {

    public static final String EMAIL_ALREADY_IN_USE = "Email address already in use";
    public static final String ACCOUNT_LOCKED = "Account locked";
    public static final String INVALID_AUTHENTICATION = "Invalid Authentication";
    public static final String USER_NOT_FOUND = "User not found";

    public static final String NAME_ALREADY_IN_USE = "Name already in use";
    public static final String EXERCISE_NOT_FOUND = "Exercise not found";
    public static final String EXERCISE_IN_USE = "Exercise is used in an active routine";
    public static final String ROUTINE_NOT_FOUND = "Routine not found";
    public static final String DUPLICATE_EXERCISE_IDS = "Duplicate exercise IDs";

    public static final String SESSION_NOT_FOUND = "Session not found";
    public static final String SESSION_NOT_PLANNED = "Session is not planned";
    public static final String SESSION_NOT_ACTIVE = "Session is not active";
    public static final String SESSION_NOT_COMPLETED = "Session is not completed or already deleted";
    public static final String NO_ACTIVE_SESSION = "No active session";
    public static final String SESSION_ALREADY_ACTIVE = "A session is already active";
    public static final String SESSION_WITHOUT_SETS = "Session contains no sets";
    public static final String SESSION_EXERCISE_NOT_FOUND = "Session exercise not found";
    public static final String SET_NOT_FOUND = "Set not found";
    public static final String SET_NUMBER_OUT_OF_RANGE = "Set number out of range";
    public static final String PLANNED_DATE_IN_PAST = "Planned date must not be in the past";

    public static final String JWT_SECRET_TOO_SHORT = "must be at least 32 characters (256 bits) long";

    public static final String VALIDATION_ERROR = "Validation error";
    public static final String UNEXPECTED_ERROR = "Unexpected internal server error";
    public static final String UNAUTHORIZED = "Unauthorized access";
    public static final String ACCESS_DENIED = "Access denied";

    private ErrorMessages() {
    }

}
