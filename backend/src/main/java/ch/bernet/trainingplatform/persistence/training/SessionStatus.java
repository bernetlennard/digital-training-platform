package ch.bernet.trainingplatform.persistence.training;

import lombok.Getter;

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
        // TODO: passende Konstante suchen, sonst IllegalArgumentException
        return null;
    }

}
