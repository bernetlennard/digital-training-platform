package ch.bernet.trainingplatform.persistence.training;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "exercise_set")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int setNumber;
    private BigDecimal weightKg;
    private int repetitions;
    private Integer restSeconds;

    public ExerciseSet(int setNumber, BigDecimal weightKg, int repetitions, Integer restSeconds) {
        this.setNumber = setNumber;
        this.weightKg = weightKg;
        this.repetitions = repetitions;
        this.restSeconds = restSeconds;
    }

}
