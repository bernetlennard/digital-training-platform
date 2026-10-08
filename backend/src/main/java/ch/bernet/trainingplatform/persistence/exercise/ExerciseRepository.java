package ch.bernet.trainingplatform.persistence.exercise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // TODO: @Query, nur deleted = false (sonst ignoriert Spring Data das "Active" im Namen)
    public List<Exercise> findActiveByUserId(Long userId);

    // TODO: @Query, nur deleted = false
    public Optional<Exercise> findActiveByIdAndUserId(Long id, Long userId);

    // TODO: @Query, nur deleted = false und Name ohne Gross-/Kleinschreibung vergleichen
    public Optional<Exercise> findActiveByUserIdAndName(Long userId, String name);

    public List<Exercise> findAllByIdInAndUserId(Collection<Long> ids, Long userId);

}
