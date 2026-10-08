package ch.bernet.trainingplatform.persistence.training;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long routineId;

    @Convert(converter = SessionStatusConverter.class)
    @Column(name = "status_id")
    private SessionStatus status;

    private LocalDate plannedDate;
    private Instant startedAt;
    private Instant finishedAt;

    @Setter
    private String note;

    private boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "training_session_id", nullable = false)
    @OrderBy("position")
    private List<SessionExercise> exercises = new ArrayList<>();

    // plannedDate null = spontane Einheit
    public TrainingSession(Long userId, Long routineId, LocalDate plannedDate) {
        this.userId = userId;
        this.routineId = routineId;
        this.plannedDate = plannedDate;
        this.status = SessionStatus.PLANNED;
    }

    public void start(Instant now, List<Long> exerciseIds) {
        // TODO: nur aus PLANNED; Übungsliste der Routine kopieren (#FA-3.5), startedAt setzen, Status ACTIVE
    }

    public void finish(Instant now) {
        // TODO: nur aus ACTIVE; mind. ein Satz (#FA-5.7), Übungen ohne Satz entfernen, finishedAt setzen, Status COMPLETED
    }

    public void revertToPlanned() {
        // TODO: Abbruch einer geplanten Einheit: Übungen und startedAt entfernen, Status PLANNED (#FA-5.8)
    }

    public void reschedule(Long routineId, LocalDate plannedDate) {
        // TODO: nur im Status PLANNED; Routine und Datum ändern
    }

    public void markDeleted() {
        // TODO: nur im Status COMPLETED erlaubt (#FA-6.5)
    }

    public boolean isSpontaneous() {
        // TODO: true, wenn plannedDate null ist
        return false;
    }

    public SessionExercise getExercise(Long sessionExerciseId) {
        // TODO: Übung der Einheit suchen, sonst NotFoundException
        return null;
    }

    public SessionExercise getExerciseOfSet(Long setId) {
        // TODO: Übung suchen, die den Satz enthält, sonst NotFoundException
        return null;
    }

}
