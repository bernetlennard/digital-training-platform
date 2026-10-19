package ch.bernet.trainingplatform.persistence.exercise;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // @Query, nur deleted = false (sonst ignoriert Spring Data das "Active" im Namen)
    @Query("SELECT e FROM Exercise e WHERE e.deleted = false AND e.userId = ?1")
    List<Exercise> findActiveByUserId(Long userId);

    // @Query, nur deleted = false
    @Query("SELECT e FROM Exercise e WHERE e.deleted = false AND e.id = ?1 AND e.userId = ?2")
    Optional<Exercise> findActiveByIdAndUserId(Long id, Long userId);

    // @Query, nur deleted = false und Name ohne Gross-/Kleinschreibung vergleichen
    @Query("SELECT e FROM Exercise e WHERE e.deleted = false AND e.userId = ?1 AND LOWER(e.name) = LOWER(?2)")
    Optional<Exercise> findActiveByUserIdAndName(Long userId, String name);

    List<Exercise> findAllByIdInAndUserId(Collection<Long> ids, Long userId);

}
