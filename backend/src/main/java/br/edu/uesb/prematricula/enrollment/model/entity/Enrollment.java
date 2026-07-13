package br.edu.uesb.prematricula.enrollment.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;
import br.edu.uesb.prematricula.student.model.entity.Student;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_enrollment_process", columnNames = {
                                "student_id",
                                "enrollment_process_id"
                })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "student_id", nullable = false)
        private Student student;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "enrollment_process_id", nullable = false)
        private EnrollmentProcess enrollmentProcess;

        @OneToMany(mappedBy = "enrollment", cascade = {
                        CascadeType.PERSIST,
                        CascadeType.MERGE
        }, orphanRemoval = true)
        @Builder.Default
        private List<EnrollmentItem> items = new ArrayList<>();

        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @Column(nullable = false)
        private boolean active;

        @PrePersist
        public void prePersist() {

                LocalDateTime now = LocalDateTime.now();

                createdAt = now;
                updatedAt = now;
                active = true;
        }

        @PreUpdate
        public void preUpdate() {
                updatedAt = LocalDateTime.now();
        }

        public void addItem(EnrollmentItem item) {

                items.add(item);
                item.setEnrollment(this);
        }

        public void removeItem(EnrollmentItem item) {

                if (items.remove(item)) {
                        item.setEnrollment(null);
                }
        }

        public void clearItems() {

                items.forEach(item -> item.setEnrollment(null));
                items.clear();
        }
}