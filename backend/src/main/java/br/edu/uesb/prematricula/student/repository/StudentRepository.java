package br.edu.uesb.prematricula.student.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.student.model.entity.Student;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

}