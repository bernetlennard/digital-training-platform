package ch.bernet.trainingplatform.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    public Optional<AppUser> findByEmail(String email);

}
