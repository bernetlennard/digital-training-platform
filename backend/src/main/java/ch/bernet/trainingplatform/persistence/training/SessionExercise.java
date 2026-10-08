package ch.bernet.trainingplatform.persistence.training;

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

    public void addSet(BigDecimal weightKg, int repetitions, Integer restSeconds) {
        // TODO: neuen Satz mit Satznummer n + 1 anhängen
    }

    public void updateSet(Long setId, BigDecimal weightKg, int repetitions, Integer restSeconds) {
        // TODO: Satz suchen (sonst NotFoundException) und Werte überschreiben
    }

    public void moveSet(Long setId, int newSetNumber) {
        // TODO: Satz an newSetNumber verschieben, danach renumberSets (#FA-5.4)
    }

    public void removeSet(Long setId) {
        // TODO: Satz entfernen (sonst NotFoundException), danach renumberSets
    }

    private void renumberSets() {
        // TODO: Satznummern lückenlos 1..n setzen
    }

}
