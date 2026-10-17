package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.ChangeEmailRequest;
import ch.bernet.trainingplatform.service.user.dto.ChangePasswordRequest;
import ch.bernet.trainingplatform.service.user.dto.DeleteAccountRequest;
import ch.bernet.trainingplatform.service.user.dto.UpdateProfileRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;
    private final JwtCookieService jwtCookieService;

    public UserController(UserService userService, JwtCookieService jwtCookieService) {
        this.userService = userService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping
    public UserResponse getProfile(@AuthenticationPrincipal Long userId) {
        return userService.getProfile(userId);
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal Long userId,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        // userService.updateProfile aufrufen
        UserResponse userResponse = userService.updateProfile(userId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(userResponse);
    }

    @PutMapping("/email")
    public ResponseEntity<UserResponse> changeEmail(@AuthenticationPrincipal Long userId,
                                    @Valid @RequestBody ChangeEmailRequest request) {
        // userService.changeEmail aufrufen, 200 mit Auth-Cookie (Set-Cookie)
        UserResponse userResponse = userService.changeEmail(userId, request);
        ResponseCookie cookie = jwtCookieService.createAuthCookie(userResponse.id());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userResponse);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Long userId,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        // userService.changePassword, 204 mit Auth-Cookie (Set-Cookie)
        userService.changePassword(userId, request);
        ResponseCookie cookie = jwtCookieService.createAuthCookie(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(null);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Long userId,
                                              @Valid @RequestBody DeleteAccountRequest request) {
        // userService.deleteAccount, 204 mit Logout-Cookie (Set-Cookie)
        userService.deleteAccount(userId, request);
        ResponseCookie cookie = jwtCookieService.createLogoutCookie();

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(null);
    }

}
