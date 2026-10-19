package ch.bernet.trainingplatform.persistence.training;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SessionStatus {

    PLANNED(1, "geplant"),
    ACTIVE(2, "aktiv"),
    COMPLETED(3, "abgeschlossen");

    private final int id;
    private final String label;

    SessionStatus(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static SessionStatus fromId(int id) {
        // passende Konstante suchen, sonst IllegalArgumentException
        return Arrays.stream(values())
                .filter(s -> s.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No SessionStatus with id " + id));
    }

}
