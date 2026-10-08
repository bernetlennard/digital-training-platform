package ch.bernet.trainingplatform.controller.user;

import ch.bernet.trainingplatform.service.user.UserService;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public UserResponse getProfile(@PathVariable Long id) {
        return userService.getProfile(id);
    }

}
