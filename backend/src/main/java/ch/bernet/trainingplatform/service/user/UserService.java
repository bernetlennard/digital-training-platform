package ch.bernet.trainingplatform.service.user;

import ch.bernet.trainingplatform.persistence.user.AppUser;
import ch.bernet.trainingplatform.persistence.user.AppUserRepository;
import ch.bernet.trainingplatform.service.user.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public UserResponse getProfile(Long id) {
        AppUser appUser = appUserRepository.findById(id).orElse(null);
        if (appUser == null) {
            return null;
        }
        return new UserResponse(appUser.getId(), appUser.getEmail(), appUser.getFirstName(), appUser.getLastName());
    }

}
