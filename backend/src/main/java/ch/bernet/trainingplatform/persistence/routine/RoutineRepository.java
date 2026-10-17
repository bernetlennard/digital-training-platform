package ch.bernet.trainingplatform.persistence.routine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

    // TODO: @Query, nur deleted = false (sonst ignoriert Spring Data das "Active" im Namen)
    public List<Routine> findActiveByUserId(Long userId);

    // TODO: @Query, nur deleted = false
    public Optional<Routine> findActiveByIdAndUserId(Long id, Long userId);

    // TODO: @Query, nur deleted = false und Name ohne Gross-/Kleinschreibung vergleichen
    public Optional<Routine> findActiveByUserIdAndName(Long userId, String name);

    public List<Routine> findAllByUserId(Long userId);

    // TODO: @Query, true wenn eine nicht gelöschte Routine die Übung enthält (#FA-2.6)
    public boolean existsActiveWithExercise(Long userId, Long exerciseId);

}
