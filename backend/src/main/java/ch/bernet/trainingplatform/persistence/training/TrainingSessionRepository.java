package ch.bernet.trainingplatform.persistence.training;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    // @Query, nur id und userId
    @Query("SELECT s FROM TrainingSession s WHERE s.id = ?1 AND s.userId = ?2")
    Optional<TrainingSession> findByIdAndUserId(Long id, Long userId);

    // @Query, nur userId und Status
    @Query("SELECT s FROM TrainingSession s WHERE s.userId = ?1 AND s.status = ?2")
    List<TrainingSession> findByUserIdAndStatus(Long userId, SessionStatus status);

    // @Query, nur Status COMPLETED und deleted = false
    @Query("SELECT s FROM TrainingSession s WHERE s.userId = ?1 AND s.routineId = ?2 AND s.deleted = false AND s.status = ch.bernet.trainingplatform.persistence.training.SessionStatus.COMPLETED")
    List<TrainingSession> findCompletedByUserIdAndRoutineId(Long userId, Long routineId);

    // @Query, routineIds aller abgeschlossenen, nicht gelöschten Einheiten
    @Query("SELECT DISTINCT s.routineId FROM TrainingSession s WHERE s.userId = ?1 AND s.deleted = false AND s.status = ch.bernet.trainingplatform.persistence.training.SessionStatus.COMPLETED")
    List<Long> findCompletedRoutineIds(Long userId);

    // @Modifying + @Query, nur Einheiten im Status PLANNED löschen
    @Modifying
    @Query("DELETE FROM TrainingSession s WHERE s.userId = ?1 AND s.routineId = ?2 AND s.status = ch.bernet.trainingplatform.persistence.training.SessionStatus.PLANNED")
    void deletePlannedByRoutineId(Long userId, Long routineId);

}
