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

/**
 * Serviço responsável pelo gerenciamento de estudantes do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas ao cadastro,
 * consulta e gerenciamento de dados dos estudantes,
 * mantendo a integridade entre as entidades de Usuário e Estudante.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserService userService;

    /**
     * Cadastra um novo estudante no sistema.
     *
     * <p>
     * Realiza a integração entre os serviços de usuário e estudante,
     * criando um usuário com role STUDENT e vinculando-o à entidade Estudante.
     * Valida a unicidade do número de matrícula.
     * </p>
     *
     * @param dto dados necessários para criação do estudante,
     *            contendo nome completo, email e número de matrícula
     * @return estudante criado com seus dados associados
     * @throws RegistrationNumberAlreadyExistsException
     *                                                  quando o número de matrícula
     *                                                  já existir
     */
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

    /**
     * Lista todos os estudantes cadastrados.
     *
     * @return lista contendo todos os estudantes
     */
    public List<StudentResponseDTO> findAll() {

        return studentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Busca um estudante pelo identificador.
     *
     * @param id identificador do estudante
     * @return estudante encontrado
     * @throws StudentNotFoundException
     *                                  quando o identificador não existir
     */
    public StudentResponseDTO findById(UUID id) {

        Student student = getStudent(id);

        return toResponse(student);
    }

    /**
     * Busca um estudante pelo identificador, retornando a entidade.
     *
     * <p>
     * Diferentemente de {@link #findById(UUID)}, este método retorna
     * a entidade JPA diretamente, útil para operações internas que
     * necessitam da entidade completa.
     * </p>
     *
     * @param id identificador do estudante
     * @return entidade de estudante encontrada
     * @throws StudentNotFoundException
     *                                  quando o identificador não existir
     */
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

    /**
     * Busca um estudante pelo número de matrícula.
     *
     * @param registrationNumber número de matrícula do estudante
     * @return entidade de estudante encontrada
     * @throws StudentNotFoundException
     *                                  quando o número de matrícula não existir
     */
    public Student findByRegistrationNumber(String registrationNumber) {
        return studentRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
    }

    /**
     * Busca um estudante pela entidade de usuário associada.
     *
     * @param user entidade de usuário associada ao estudante
     * @return entidade de estudante encontrada
     * @throws StudentNotFoundException
     *                                  quando nenhum estudante estiver associado ao
     *                                  usuário
     */
    public Student findByUser(User user) {

        return studentRepository.findByUser(user)
                .orElseThrow(() -> new StudentNotFoundException("Student not found."));
    }

}