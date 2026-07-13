package br.edu.uesb.prematricula.enrollmentprocessclasses.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.edu.uesb.prematricula.classgroup.model.entity.ClassGroup;
import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "enrollment_process_classes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_enrollment_process_class",
            columnNames = {
                "enrollment_process_id",
                "class_group_id"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentProcessClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "enrollment_process_id",
        nullable = false
    )
    private EnrollmentProcess enrollmentProcess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "class_group_id",
        nullable = false
    )
    private ClassGroup classGroup;

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false
    )
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        active = true;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}