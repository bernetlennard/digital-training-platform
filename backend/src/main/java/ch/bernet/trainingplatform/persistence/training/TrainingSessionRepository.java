package ch.bernet.trainingplatform.persistence.training;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    /*// TODO: prüfen, ob gelöschte Einheiten (deleted = true) ausgeschlossen werden müssen
    public Optional<TrainingSession> findByIdAndUserId(Long id, Long userId);

    public List<TrainingSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    // TODO: @Query, nur Status COMPLETED und deleted = false
    public List<TrainingSession> findCompletedByUserIdAndRoutineId(Long userId, Long routineId);

    // TODO: @Query, routineIds aller abgeschlossenen, nicht gelöschten Einheiten
    public Set<Long> findCompletedRoutineIds(Long userId);

    // TODO: @Modifying + @Query, nur Einheiten im Status PLANNED löschen
    public void deletePlannedByRoutineId(Long routineId);
*/
}
