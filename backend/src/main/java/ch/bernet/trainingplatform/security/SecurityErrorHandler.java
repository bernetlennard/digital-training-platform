package ch.bernet.trainingplatform.security;

import ch.bernet.trainingplatform.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityErrorHandler.class);
    private final JsonMapper jsonMapper;

    public SecurityErrorHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        // TODO: Zugriff ohne Token-Inhalt loggen (#NFA-1.7), dann writeError mit 401
        log.warn("Unauthorized access attempt: {} {}", request.getMethod(), request.getRequestURI());
        writeError(response, HttpStatus.UNAUTHORIZED, "Unautorisierter Zugriff");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        // TODO: Zugriff ohne Token-Inhalt loggen (#NFA-1.7), dann writeError mit 403
        log.warn("Access denied: {} {}", request.getMethod(), request.getRequestURI());
        writeError(response, HttpStatus.FORBIDDEN, "Zugriff verweigert");
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        // TODO: ErrorResponse als JSON in die Response schreiben (L10)
        ErrorResponse body = new ErrorResponse(status.value(), message, List.of());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getWriter(), body);
    }

}
