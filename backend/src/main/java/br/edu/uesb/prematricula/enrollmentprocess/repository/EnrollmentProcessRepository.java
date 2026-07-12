package br.edu.uesb.prematricula.enrollmentprocess.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;
import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;

public interface EnrollmentProcessRepository
        extends JpaRepository<EnrollmentProcess, UUID> {

    boolean existsByAcademicPeriod(AcademicPeriod academicPeriod);

    Optional<EnrollmentProcess> findByAcademicPeriod(AcademicPeriod academicPeriod);

    boolean existsByActiveTrue();

    List<EnrollmentProcess> findByActiveTrue();
}
