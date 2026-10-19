package ch.bernet.trainingplatform.persistence.training;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, Long> {

    // @Query, Sätze der letzten abgeschlossenen, nicht gelöschten Einheit derselben Routine, in der die Übung
    // vorkam (#FA-5.5). Übungen ohne Satz entfernt TrainingSession.finish, ausgelassene zählen also nicht.
    @Query("""
        SELECT st FROM TrainingSession s
        JOIN s.exercises e
        JOIN e.sets st
        WHERE s.userId = ?1 AND s.routineId = ?2 AND e.exerciseId = ?3
          AND s.status = ch.bernet.trainingplatform.persistence.training.SessionStatus.COMPLETED
          AND s.deleted = false
          AND s.finishedAt = (
              SELECT MAX(s2.finishedAt) FROM TrainingSession s2
              JOIN s2.exercises e2
              WHERE s2.userId = ?1 AND s2.routineId = ?2 AND e2.exerciseId = ?3
                AND s2.status = ch.bernet.trainingplatform.persistence.training.SessionStatus.COMPLETED
                AND s2.deleted = false)
        ORDER BY st.setNumber
        """)
    public List<ExerciseSet> findLastCompletedSets(Long userId, Long routineId, Long exerciseId);

}
