package br.edu.uesb.prematricula.enrollment.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import br.edu.uesb.prematricula.enrollmentprocessclasses.model.entity.EnrollmentProcessClass;

@Entity
@Table(
        name = "enrollment_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_enrollment_item_process_class",
                        columnNames = {
                                "enrollment_id",
                                "enrollment_process_class_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "enrollment_id",
            nullable = false
    )
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "enrollment_process_class_id",
            nullable = false
    )
    private EnrollmentProcessClass enrollmentProcessClass;

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

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
