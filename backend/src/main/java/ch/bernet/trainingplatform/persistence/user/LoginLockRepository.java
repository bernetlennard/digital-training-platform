package ch.bernet.trainingplatform.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginLockRepository extends JpaRepository<LoginLock, Long> {

    public Optional<LoginLock> findByEmail(String email);

    public void deleteByEmail(String email);

}
