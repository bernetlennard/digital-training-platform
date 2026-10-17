package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.common.ApiStatus;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.LoginRequest;
import ch.bernet.trainingplatform.service.user.dto.RegisterRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentifizierung")
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtCookieService jwtCookieService;

    public AuthController(UserService userService, JwtCookieService jwtCookieService) {
        this.userService = userService;
        this.jwtCookieService = jwtCookieService;
    }

    @Operation(summary = "Konto anlegen und anmelden")
    @ApiResponse(responseCode = "201", description = ApiStatus.CREATED)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = userService.register(request);
        ResponseCookie cookie = jwtCookieService.createAuthCookie(userResponse.id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userResponse);
    }

    @Operation(summary = "Anmelden")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "401", description = ApiStatus.UNAUTHORIZED)
    @ApiResponse(responseCode = "429", description = ApiStatus.TOO_MANY_REQUESTS)
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        // TODO: authentifizieren, 200 mit Auth-Cookie (Set-Cookie) zurückgeben

        UserResponse userResponse = userService.authenticate(request);
        ResponseCookie cookie = jwtCookieService.createAuthCookie(userResponse.id());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userResponse);
    }

    @Operation(summary = "Abmelden, Cookie löschen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // TODO: 204 mit Logout-Cookie (Set-Cookie) zurückgeben

        ResponseCookie cookie = jwtCookieService.createLogoutCookie();

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(null);
    }

}
