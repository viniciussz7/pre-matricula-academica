package br.edu.uesb.prematricula.student.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uesb.prematricula.student.exception.RegistrationNumberAlreadyExistsException;
import br.edu.uesb.prematricula.student.exception.StudentNotFoundException;
import br.edu.uesb.prematricula.student.model.dto.request.CreateStudentRequestDTO;
import br.edu.uesb.prematricula.student.model.dto.response.StudentResponseDTO;
import br.edu.uesb.prematricula.student.model.entity.Student;
import br.edu.uesb.prematricula.student.repository.StudentRepository;
import br.edu.uesb.prematricula.user.model.dto.request.CreateUserRequestDTO;
import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.model.enums.UserRole;
import br.edu.uesb.prematricula.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserService userService;

    @Transactional
    public StudentResponseDTO create(CreateStudentRequestDTO dto) {

        validateRegistrationNumber(dto.registrationNumber());

        CreateUserRequestDTO userDTO = new CreateUserRequestDTO(
                dto.fullName(),
                dto.email(),
                UserRole.STUDENT);

        User user = userService.createUser(userDTO);

        Student student = Student.builder()
                .user(user)
                .registrationNumber(dto.registrationNumber())
                .build();

        Student savedStudent = studentRepository.save(student);

        return toResponse(savedStudent);
    }

    public List<StudentResponseDTO> findAll() {

        return studentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentResponseDTO findById(UUID id) {

        Student student = getStudent(id);

        return toResponse(student);
    }

    public Student getStudent(UUID id) {

        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("Student not found."));
    }

    // update()?

    private StudentResponseDTO toResponse(Student student) {

        return new StudentResponseDTO(
                student.getId(),
                student.getUser().getId(),
                student.getUser().getFullName(),
                student.getUser().getEmail(),
                student.getRegistrationNumber(),
                student.isActive()

        );

    }

    private void validateRegistrationNumber(String registrationNumber) {

        if (studentRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new RegistrationNumberAlreadyExistsException(
                    "Registration number already exists.");
        }

    }

    public Student findByRegistrationNumber(String registrationNumber) {
        return studentRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
    }

    public Student findByUser(User user) {

        return studentRepository.findByUser(user)
                .orElseThrow(() -> new StudentNotFoundException("Student not found."));
    }

}