package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.common.ApiStatus;
import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.ChangeEmailRequest;
import ch.bernet.trainingplatform.service.user.dto.ChangePasswordRequest;
import ch.bernet.trainingplatform.service.user.dto.DeleteAccountRequest;
import ch.bernet.trainingplatform.service.user.dto.UpdateProfileRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Benutzerkonto")
@RestController
@RequestMapping(path = "/api/v{version}/users/me", version = "1")
public class UserController {

    private final UserService userService;
    private final JwtCookieService jwtCookieService;

    public UserController(UserService userService, JwtCookieService jwtCookieService) {
        this.userService = userService;
        this.jwtCookieService = jwtCookieService;
    }

    @Operation(summary = "Eigenes Profil lesen")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @GetMapping
    public UserResponse getProfile(@AuthenticationPrincipal Long userId) {
        return userService.getProfile(userId);
    }

    @Operation(summary = "Vor- und Nachname ändern")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @PutMapping
    public UserResponse updateProfile(@AuthenticationPrincipal Long userId,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        // userService.updateProfile aufrufen
        return userService.updateProfile(userId, request);
    }

    @Operation(summary = "E-Mail-Adresse ändern")
    @ApiResponse(responseCode = "200", description = ApiStatus.OK)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "401", description = ApiStatus.UNAUTHORIZED)
    @ApiResponse(responseCode = "409", description = ApiStatus.CONFLICT)
    @PutMapping("/email")
    public ResponseEntity<UserResponse> changeEmail(@AuthenticationPrincipal Long userId,
                                                    @Valid @RequestBody ChangeEmailRequest request) {
        // userService.changeEmail aufrufen, 200 mit Auth-Cookie (Set-Cookie)
        UserResponse userResponse = userService.changeEmail(userId, request);
        ResponseCookie cookie = jwtCookieService.createAuthCookie(userResponse.id());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userResponse);
    }

    @Operation(summary = "Passwort ändern")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "401", description = ApiStatus.UNAUTHORIZED)
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Long userId,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        // userService.changePassword, 204 mit Auth-Cookie (Set-Cookie)
        userService.changePassword(userId, request);
        ResponseCookie cookie = jwtCookieService.createAuthCookie(userId);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @Operation(summary = "Konto mit allen Daten löschen")
    @ApiResponse(responseCode = "204", description = ApiStatus.NO_CONTENT)
    @ApiResponse(responseCode = "400", description = ApiStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "401", description = ApiStatus.UNAUTHORIZED)
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Long userId,
                                              @Valid @RequestBody DeleteAccountRequest request) {
        // userService.deleteAccount, 204 mit Logout-Cookie (Set-Cookie)
        userService.deleteAccount(userId, request);
        ResponseCookie cookie = jwtCookieService.createLogoutCookie();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

}
