package br.edu.uesb.prematricula.enrollmentprocess.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;
import br.edu.uesb.prematricula.enrollmentprocess.model.enums.EnrollmentProcessStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enrollment_processes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriod academicPeriod;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (!active) {
            active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public EnrollmentProcessStatus getStatus() {

        if (!hasStarted()) {
            return EnrollmentProcessStatus.NOT_STARTED;
        }

        if (isFinished()) {
            return EnrollmentProcessStatus.FINISHED;
        }

        return EnrollmentProcessStatus.OPEN;
    }

    public boolean isOpen() {
        return active
            && getStatus() == EnrollmentProcessStatus.OPEN;
    }

    public boolean isFinished() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean hasStarted() {
        return !LocalDateTime.now().isBefore(startDate);
    }
}