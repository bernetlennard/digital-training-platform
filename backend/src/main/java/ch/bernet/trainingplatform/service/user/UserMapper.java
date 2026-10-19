package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.persistence.user.AppUser;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(AppUser user) {
        // AppUser auf UserResponse abbilden
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());
    }

}
