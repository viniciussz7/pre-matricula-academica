package br.edu.uesb.prematricula.enrollment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.enrollment.model.entity.Enrollment;
import br.edu.uesb.prematricula.enrollment.model.entity.EnrollmentItem;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.entity.EnrollmentProcessClass;

public interface EnrollmentItemRepository
        extends JpaRepository<EnrollmentItem, UUID> {

    List<EnrollmentItem> findByEnrollment(
            Enrollment enrollment
    );

    Optional<EnrollmentItem> findByIdAndEnrollment(
            UUID id,
            Enrollment enrollment
    );

    boolean existsByEnrollmentAndEnrollmentProcessClass(
            Enrollment enrollment,
            EnrollmentProcessClass enrollmentProcessClass
    );

    long countByEnrollmentProcessClassAndEnrollmentActiveTrue(
            EnrollmentProcessClass enrollmentProcessClass
    );
}