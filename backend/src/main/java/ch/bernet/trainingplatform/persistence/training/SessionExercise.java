package ch.bernet.trainingplatform.persistence.training;

import ch.bernet.trainingplatform.common.exception.BusinessRuleException;
import ch.bernet.trainingplatform.common.exception.ErrorMessages;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "session_exercise")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long exerciseId;
    private int position;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "session_exercise_id", nullable = false)
    @OrderBy("setNumber")
    private List<ExerciseSet> sets = new ArrayList<>();

    public SessionExercise(Long exerciseId, int position) {
        this.exerciseId = exerciseId;
        this.position = position;
    }

    public ExerciseSet addSet(BigDecimal weightKg, int repetitions, Integer restSeconds) {
        // neuen Satz mit Satznummer n + 1 anhängen und zurückgeben
        ExerciseSet set = new ExerciseSet(sets.size() + 1, weightKg, repetitions, restSeconds);
        sets.add(set);
        return set;
    }

    public void updateSet(Long setId, BigDecimal weightKg, int repetitions, Integer restSeconds) {
        // Satz suchen (sonst NotFoundException) und Werte überschreiben
        ExerciseSet exerciseSet = findSet(setId);
        exerciseSet.setWeightKg(weightKg);
        exerciseSet.setRepetitions(repetitions);
        exerciseSet.setRestSeconds(restSeconds);
    }

    public void moveSet(Long setId, int newSetNumber) {
        // Satz an newSetNumber verschieben, danach renumberSets
        ExerciseSet exerciseSet = findSet(setId);
        if (newSetNumber < 1 || newSetNumber > sets.size()) {
            throw new BusinessRuleException(ErrorMessages.SET_NUMBER_OUT_OF_RANGE);
        }
        sets.remove(exerciseSet);
        sets.add(newSetNumber - 1, exerciseSet);
        renumberSets();
    }

    public void removeSet(Long setId) {
        // Satz entfernen (sonst NotFoundException), danach renumberSets
        boolean isRemoved = sets.removeIf(s -> setId.equals(s.getId()));
        if (!isRemoved) {
            throw new NotFoundException(ErrorMessages.SET_NOT_FOUND);
        }
        renumberSets();
    }

    private ExerciseSet findSet(Long setId) {
        return sets.stream()
                .filter(s -> setId.equals(s.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessages.SET_NOT_FOUND));
    }

    private void renumberSets() {
        // Satznummern lückenlos 1..n setzen
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).setSetNumber(i + 1);
        }
    }

}
