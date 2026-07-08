package br.edu.uesb.prematricula.student.service;

import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.student.exception.StudentNotFoundException;
import br.edu.uesb.prematricula.student.model.entity.Student;
import br.edu.uesb.prematricula.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public Student findByRegistrationNumber(String registrationNumber) {
        return studentRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
    }

}