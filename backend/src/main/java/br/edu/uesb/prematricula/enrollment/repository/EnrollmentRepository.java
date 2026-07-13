package br.edu.uesb.prematricula.enrollment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.enrollment.model.entity.Enrollment;
import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;
import br.edu.uesb.prematricula.student.model.entity.Student;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByStudentAndEnrollmentProcessAndActiveTrue(
            Student student,
            EnrollmentProcess enrollmentProcess);

    Optional<Enrollment> findByStudentAndEnrollmentProcess(
            Student student,
            EnrollmentProcess enrollmentProcess);

    Optional<Enrollment> findByIdAndActiveTrue(UUID id);

    Optional<Enrollment> findByStudentAndEnrollmentProcessAndActiveTrue(
            Student student,
            EnrollmentProcess enrollmentProcess);

    List<Enrollment> findByEnrollmentProcessAndActiveTrue(
            EnrollmentProcess enrollmentProcess);

    List<Enrollment> findByEnrollmentProcess(
            EnrollmentProcess enrollmentProcess);

    List<Enrollment> findByStudentOrderByEnrollmentProcessAcademicPeriodCodeDesc(
            Student student);


}