package ch.bernet.trainingplatform.persistence.exercise;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "exercise")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String name;

    @Convert(converter = MuscleGroupConverter.class)
    @Column(name = "muscle_group_id")
    private MuscleGroup muscleGroup;

    private String description;
    private boolean deleted;

    public Exercise(Long userId, String name, MuscleGroup muscleGroup, String description) {
        this.userId = userId;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.description = description;
    }

}
