package ch.bernet.trainingplatform.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginLockRepository extends JpaRepository<LoginLock, Long> {

    Optional<LoginLock> findByEmail(String email);

    void deleteByEmail(String email);

}
