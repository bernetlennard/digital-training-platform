package ch.bernet.trainingplatform.persistence.training;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, Long> {

    // TODO: @Query, Sätze der letzten abgeschlossenen, nicht gelöschten Einheit, in der die Übung vorkam (#FA-5.5)
   // public List<ExerciseSet> findLastCompletedSets(Long userId, Long exerciseId);

}
