package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.LoginRequest;
import ch.bernet.trainingplatform.service.user.dto.RegisterRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtCookieService jwtCookieService;

    public AuthController(UserService userService, JwtCookieService jwtCookieService) {
        this.userService = userService;
        this.jwtCookieService = jwtCookieService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        // TODO: registrieren, 201 mit Auth-Cookie (Set-Cookie) zurückgeben
        return null;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        // TODO: authentifizieren, 200 mit Auth-Cookie (Set-Cookie) zurückgeben
        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // TODO: 204 mit Logout-Cookie (Set-Cookie) zurückgeben
        return null;
    }

}
