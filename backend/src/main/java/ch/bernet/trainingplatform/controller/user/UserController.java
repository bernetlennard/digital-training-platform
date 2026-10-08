package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.security.JwtCookieService;
import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.ChangeEmailRequest;
import ch.bernet.trainingplatform.service.user.dto.ChangePasswordRequest;
import ch.bernet.trainingplatform.service.user.dto.DeleteAccountRequest;
import ch.bernet.trainingplatform.service.user.dto.UpdateProfileRequest;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import jakarta.validation.Valid;
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
    public UserResponse updateProfile(@AuthenticationPrincipal Long userId,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        // TODO: userService.updateProfile aufrufen
        return null;
    }

    @PutMapping("/email")
    public UserResponse changeEmail(@AuthenticationPrincipal Long userId,
                                    @Valid @RequestBody ChangeEmailRequest request) {
        // TODO: userService.changeEmail aufrufen
        return null;
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Long userId,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        // TODO: userService.changePassword, 204 (klären, ob ein neues Cookie gesetzt werden soll)
        return null;
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Long userId,
                                              @Valid @RequestBody DeleteAccountRequest request) {
        // TODO: userService.deleteAccount, 204 mit Logout-Cookie (Set-Cookie)
        return null;
    }

}
