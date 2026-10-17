package ch.bernet.trainingplatform.persistence.routine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    // @Query, nur deleted = false (sonst ignoriert Spring Data das "Active" im Namen)
    @Query("SELECT r FROM Routine r WHERE r.deleted = false AND r.userId = ?1")
    public List<Routine> findActiveByUserId(Long userId);

    // @Query, nur deleted = false
    @Query("SELECT r FROM Routine r WHERE r.deleted = false AND r.id = ?1 AND r.userId = ?2")
    public Optional<Routine> findActiveByIdAndUserId(Long id, Long userId);

    // @Query, nur deleted = false und Name ohne Gross-/Kleinschreibung vergleichen
    @Query("SELECT r FROM Routine r WHERE r.deleted = false AND r.userId = ?1 AND LOWER(r.name) = LOWER(?2)")
    public Optional<Routine> findActiveByUserIdAndName(Long userId, String name);

    public List<Routine> findAllByUserId(Long userId);

    // @Query, true wenn eine nicht gelöschte Routine die Übung enthält (#FA-2.6)
    @Query("SELECT COUNT(r) > 0 FROM Routine r JOIN r.exercises e WHERE r.deleted = false AND r.userId = ?1 AND e.exerciseId = ?2")
    public boolean existsActiveWithExercise(Long userId, Long exerciseId);

}
