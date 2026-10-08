package ch.bernet.trainingplatform.persistence.routine;

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
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routine")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    @Setter
    private String name;

    @Setter
    private boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "routine_id", nullable = false)
    @OrderBy("position")
    private List<RoutineExercise> exercises = new ArrayList<>();

    public Routine(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public void replaceExercises(List<Long> exerciseIds) {
        // TODO: Liste leeren und pro exerciseId ein RoutineExercise mit Position 1..n anlegen
    }

}
