package br.edu.uesb.prematricula.student.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.student.model.entity.Student;
import br.edu.uesb.prematricula.user.model.entity.User;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    Optional<Student> findByUser(User user);

}