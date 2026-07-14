package br.edu.uesb.prematricula.enrollmentprocessclasses.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.classgroup.model.entity.ClassGroup;
import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.entity.EnrollmentProcessClass;

public interface EnrollmentProcessClassRepository
                extends JpaRepository<EnrollmentProcessClass, UUID> {

        boolean existsByEnrollmentProcessAndClassGroup(
                        EnrollmentProcess enrollmentProcess,
                        ClassGroup classGroup);

        List<EnrollmentProcessClass> findByEnrollmentProcessAndActiveTrue(
                        EnrollmentProcess enrollmentProcess);

        Optional<EnrollmentProcessClass> findByEnrollmentProcessAndClassGroup(
                        EnrollmentProcess enrollmentProcess,
                        ClassGroup classGroup);

}