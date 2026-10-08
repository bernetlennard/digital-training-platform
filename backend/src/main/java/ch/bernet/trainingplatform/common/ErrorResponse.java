package ch.bernet.trainingplatform.common;

import java.util.List;

public record ErrorResponse(int status, String message, List<String> details) {
}
