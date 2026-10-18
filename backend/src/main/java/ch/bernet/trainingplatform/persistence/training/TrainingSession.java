package ch.bernet.trainingplatform.persistence.training;

import ch.bernet.trainingplatform.common.exception.ConflictException;
import ch.bernet.trainingplatform.common.exception.NotFoundException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainingSession implements Comparable<TrainingSession> {

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
        // nur aus PLANNED, Übungsliste der Routine kopieren (#FA-3.5), startedAt setzen, Status ACTIVE
        if (status != SessionStatus.PLANNED) {
            throw new NotFoundException("Session is not planned.");
        }

        exercises.clear();
        for (int i = 0; i < exerciseIds.size(); i++) {
            exercises.add(new SessionExercise(exerciseIds.get(i), i + 1));
        }

        startedAt = now;
        status = SessionStatus.ACTIVE;
    }

    public void finish(Instant now) {
        // nur aus ACTIVE; mind. ein Satz (#FA-5.7), Übungen ohne Satz entfernen, finishedAt setzen, Status COMPLETED
        if (status != SessionStatus.ACTIVE) {
            throw new NotFoundException("Session is not active.");
        }

        if (exercises.stream().allMatch(e -> e.getSets().isEmpty())) {
            throw new ConflictException("Session has no exercises with sets.");
        }
        exercises.removeIf(e -> e.getSets().isEmpty());

        finishedAt = now;
        status = SessionStatus.COMPLETED;
    }

    public void revertToPlanned() {
        // Abbruch einer geplanten Einheit: Übungen und startedAt entfernen, Status PLANNED (#FA-5.8)
        if (status != SessionStatus.ACTIVE) {
            throw new NotFoundException("Session is not active.");
        }

        exercises.clear();
        startedAt = null;
        status = SessionStatus.PLANNED;
    }

    public void reschedule(Long routineId, LocalDate plannedDate) {
        // nur im Status PLANNED; Routine und Datum ändern
        if (status != SessionStatus.PLANNED) {
            throw new NotFoundException("Session is not planned.");
        }

        this.routineId = routineId;
        this.plannedDate = plannedDate;
    }

    public void markDeleted() {
        // nur im Status COMPLETED erlaubt (#FA-6.5)
        if (status != SessionStatus.COMPLETED || deleted) {
            throw new NotFoundException("Session is not completed or already deleted.");
        }

        deleted = true;
    }

    public boolean isSpontaneous() {
        // true, wenn plannedDate null ist
        return plannedDate == null;
    }

    public SessionExercise getExercise(Long sessionExerciseId) {
        // Übung der Einheit suchen, sonst NotFoundException
        return exercises.stream()
                .filter(e -> sessionExerciseId.equals(e.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("SessionExercise not found"));
    }

    public SessionExercise getExerciseOfSet(Long setId) {
        // Übung suchen, die den Satz enthält, sonst NotFoundException
        return exercises.stream().filter(e -> e.getSets().stream().anyMatch(s -> setId.equals(s.getId())))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("SessionExercise not found"));
    }

    @Override
    public int compareTo(TrainingSession trainingSession) {
        return this.plannedDate.compareTo(trainingSession.plannedDate);
    }

}
