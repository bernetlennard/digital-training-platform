package ch.bernet.trainingplatform.persistence.exercise;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
    private static final HashMap<Integer, MuscleGroup> ALL = new HashMap<>();

    static {
        for (MuscleGroup muscleGroup : values()) {
            ALL.put(muscleGroup.getId(), muscleGroup);
        }
    }

    MuscleGroup(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static MuscleGroup fromId(int id) {
        // passende Konstante suchen, sonst IllegalArgumentException
        if(ALL.containsKey(id)) {
            return ALL.get(id);
        }
        throw new IllegalArgumentException("No MuscleGroup with id " + id);
    }
}
