package ch.bernet.trainingplatform.persistence.exercise;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MuscleGroup {

    CHEST(1, "Brust"),
    BACK(2, "Rücken"),
    SHOULDERS(3, "Schultern"),
    BICEPS(4, "Bizeps"),
    TRICEPS(5, "Trizeps"),
    ABS(6, "Bauch"),
    LEGS(7, "Beine"),
    GLUTES(8, "Gesäss");

    private final int id;
    private final String label;

    MuscleGroup(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static MuscleGroup fromId(int id) {
        // passende Konstante suchen, sonst IllegalArgumentException
        return Arrays.stream(values())
                .filter(s -> s.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No MuscleGroup with id " + id));
    }
}
