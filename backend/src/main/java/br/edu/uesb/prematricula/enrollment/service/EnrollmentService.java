package br.edu.uesb.prematricula.enrollment.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uesb.prematricula.classgroup.model.entity.ClassGroup;
import br.edu.uesb.prematricula.enrollment.exception.EnrollmentAlreadyExistsException;
import br.edu.uesb.prematricula.enrollment.exception.EnrollmentNotFoundException;
import br.edu.uesb.prematricula.enrollment.exception.InvalidEnrollmentException;
import br.edu.uesb.prematricula.enrollment.model.dto.request.CreateEnrollmentRequestDTO;
import br.edu.uesb.prematricula.enrollment.model.dto.request.UpdateEnrollmentRequestDTO;
import br.edu.uesb.prematricula.enrollment.model.dto.response.EnrollmentItemResponseDTO;
import br.edu.uesb.prematricula.enrollment.model.dto.response.EnrollmentResponseDTO;
import br.edu.uesb.prematricula.enrollment.model.entity.Enrollment;
import br.edu.uesb.prematricula.enrollment.model.entity.EnrollmentItem;
import br.edu.uesb.prematricula.enrollment.repository.EnrollmentItemRepository;
import br.edu.uesb.prematricula.enrollment.repository.EnrollmentRepository;
import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;
import br.edu.uesb.prematricula.enrollmentprocess.service.EnrollmentProcessService;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.entity.EnrollmentProcessClass;
import br.edu.uesb.prematricula.enrollmentprocessclasses.service.EnrollmentProcessClassService;
import br.edu.uesb.prematricula.student.model.entity.Student;
import br.edu.uesb.prematricula.student.service.StudentService;
import br.edu.uesb.prematricula.user.model.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

        private final EnrollmentRepository enrollmentRepository;
        private final EnrollmentItemRepository enrollmentItemRepository;

        private final StudentService studentService;
        private final EnrollmentProcessService enrollmentProcessService;
        private final EnrollmentProcessClassService enrollmentProcessClassService;

        /**
         * Cria uma nova matrícula para o usuário autenticado.
         *
         * <p>
         * Realiza múltiplas validações: verifica se há processo de matrícula aberto,
         * valida IDs de seleção (não vazio, máximo 7 turmas, sem duplicatas),
         * carrega e valida as turmas selecionadas, verifica capacidade das turmas
         * e adiciona os itens à matrícula.
         * </p>
         *
         * @param authenticatedUser usuário autenticado para o qual a matrícula será
         *                          criada
         * @param dto               dados da matrícula contendo IDs de turmas
         *                          selecionadas
         * @return matrícula criada com os itens adicionados
         * @throws InvalidEnrollmentException
         *                                          quando seleção for inválida ou
         *                                          turmas indisponíveis
         * @throws EnrollmentAlreadyExistsException
         *                                          quando já existir matrícula ativa no
         *                                          período
         */
        @Transactional
        public EnrollmentResponseDTO create(
                        User authenticatedUser,
                        CreateEnrollmentRequestDTO dto) {

                Student student = studentService.findByUser(authenticatedUser);

                EnrollmentProcess process = enrollmentProcessService.getOpenProcess();

                List<UUID> selectedIds = dto.enrollmentProcessClassIds();

                validateSelectionIds(selectedIds);

                List<EnrollmentProcessClass> processClasses = loadAndValidateProcessClasses(
                                selectedIds,
                                process);

                Enrollment enrollment = prepareEnrollment(student, process);

                validateCapacity(processClasses);

                addItems(enrollment, processClasses);

                Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

                return toResponse(savedEnrollment);
        }

        /**
         * Atualiza a matrícula do usuário autenticado.
         *
         * <p>
         * Remove turmas não selecionadas e adiciona novas turmas selecionadas.
         * Valida capacidade apenas das turmas adicionadas, permitindo manter
         * turmas já inscritas mesmo que a turma esteja cheia.
         * </p>
         *
         * @param authenticatedUser usuário autenticado cuja matrícula será atualizada
         * @param enrollmentId      identificador da matrícula a ser atualizada
         * @param dto               novos dados contendo IDs de turmas selecionadas
         * @return matrícula atualizada com os itens modificados
         * @throws InvalidEnrollmentException
         *                                     quando seleção for inválida, matrícula
         *                                     for inativa,
         *                                     ou não pertencer ao processo aberto
         * @throws EnrollmentNotFoundException
         *                                     quando a matrícula não existir
         */
        @Transactional
        public EnrollmentResponseDTO update(
                        User authenticatedUser,
                        UUID enrollmentId,
                        UpdateEnrollmentRequestDTO dto) {

                Student student = studentService.findByUser(authenticatedUser);

                EnrollmentProcess openProcess = enrollmentProcessService.getOpenProcess();

                Enrollment enrollment = getActiveEnrollment(enrollmentId);

                validateEnrollmentAccess(
                                enrollment,
                                student,
                                openProcess);

                List<UUID> selectedIds = dto.enrollmentProcessClassIds();

                validateSelectionIds(selectedIds);

                List<EnrollmentProcessClass> selectedProcessClasses = loadAndValidateProcessClasses(
                                selectedIds,
                                openProcess);

                List<EnrollmentProcessClass> newProcessClasses = findNewProcessClasses(
                                enrollment,
                                selectedProcessClasses);

                /*
                 * A capacidade é validada somente para novas turmas.
                 * Uma turma que já estava na matrícula não deve ser
                 * contada novamente como uma nova ocupação.
                 */
                validateCapacity(newProcessClasses);

                Set<UUID> selectedIdSet = new HashSet<>(selectedIds);

                removeUnselectedItems(
                                enrollment,
                                selectedIdSet);

                addItems(
                                enrollment,
                                newProcessClasses);

                Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

                return toResponse(savedEnrollment);
        }

        /**
         * Cancela a matrícula do usuário autenticado.
         *
         * <p>
         * Realiza exclusão lógica marcando a matrícula como inativa.
         * Valida que a matrícula pertence ao usuário autenticado e ao
         * processo aberto antes de cancelar.
         * </p>
         *
         * @param authenticatedUser usuário autenticado cuja matrícula será cancelada
         * @param enrollmentId      identificador da matrícula a ser cancelada
         * @throws InvalidEnrollmentException
         *                                     quando matrícula não pertencer ao usuário
         *                                     ou ao processo aberto
         * @throws EnrollmentNotFoundException
         *                                     quando a matrícula ativa não existir
         */
        @Transactional
        public void cancel(
                        User authenticatedUser,
                        UUID enrollmentId) {

                Student student = studentService.findByUser(authenticatedUser);

                EnrollmentProcess openProcess = enrollmentProcessService.getOpenProcess();

                Enrollment enrollment = getActiveEnrollment(enrollmentId);

                validateEnrollmentAccess(
                                enrollment,
                                student,
                                openProcess);

                enrollment.setActive(false);

                enrollmentRepository.save(enrollment);
        }

        /**
         * Busca a matrícula ativa do usuário autenticado no processo aberto.
         *
         * @param authenticatedUser usuário autenticado
         * @return matrícula ativa do usuário no processo aberto
         * @throws EnrollmentNotFoundException
         *                                     quando nenhuma matrícula ativa for
         *                                     encontrada
         */
        @Transactional(readOnly = true)
        public EnrollmentResponseDTO findMyEnrollment(
                        User authenticatedUser) {

                Student student = studentService.findByUser(authenticatedUser);

                EnrollmentProcess process = enrollmentProcessService.getOpenProcess();

                Enrollment enrollment = enrollmentRepository
                                .findByStudentAndEnrollmentProcess(
                                                student,
                                                process)
                                .orElseThrow(() -> new EnrollmentNotFoundException(
                                                "Enrollment not found for the authenticated student in the open process."));

                return toResponse(enrollment);
        }

        /**
         * Busca uma matrícula pelo identificador.
         *
         * @param id identificador da matrícula
         * @return matrícula encontrada
         * @throws EnrollmentNotFoundException
         *                                     quando o identificador não existir
         */
        @Transactional(readOnly = true)
        public EnrollmentResponseDTO findById(UUID id) {

                Enrollment enrollment = getEnrollment(id);

                return toResponse(enrollment);
        }

        /**
         * Lista todas as matrículas de um processo de matrícula.
         *
         * @param processId identificador do processo de matrícula
         * @return lista contendo todas as matrículas do processo
         */
        @Transactional(readOnly = true)
        public List<EnrollmentResponseDTO> findByProcess(
                        UUID processId) {

                EnrollmentProcess process = enrollmentProcessService.getEnrollmentProcess(processId);

                return enrollmentRepository
                                .findByEnrollmentProcess(process)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        /**
         * Lista o histórico de matrículas do usuário autenticado.
         *
         * <p>
         * Retorna todas as matrículas (ativas e canceladas) ordenadas
         * em ordem decrescente de período acadêmico.
         * </p>
         *
         * @param authenticatedUser usuário autenticado
         * @return lista de matrículas do usuário ordenadas por período
         */
        @Transactional(readOnly = true)
        public List<EnrollmentResponseDTO> findMyHistory(
                        User authenticatedUser) {

                Student student = studentService.findByUser(authenticatedUser);

                return enrollmentRepository
                                .findByStudentOrderByEnrollmentProcessAcademicPeriodCodeDesc(
                                                student)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        /**
         * Lista o histórico de matrículas de um estudante específico.
         *
         * <p>
         * Retorna todas as matrículas do estudante (ativas e canceladas)
         * ordenadas em ordem decrescente de período acadêmico.
         * </p>
         *
         * @param studentId identificador do estudante
         * @return lista de matrículas do estudante ordenadas por período
         */
        @Transactional(readOnly = true)
        public List<EnrollmentResponseDTO> findByStudent(
                        UUID studentId) {

                Student student = studentService.getStudent(studentId);

                return enrollmentRepository
                                .findByStudentOrderByEnrollmentProcessAcademicPeriodCodeDesc(
                                                student)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        private Enrollment getEnrollment(UUID id) {

                return enrollmentRepository.findById(id)
                                .orElseThrow(() -> new EnrollmentNotFoundException(
                                                "Enrollment not found."));
        }

        private Enrollment getActiveEnrollment(UUID id) {

                return enrollmentRepository.findByIdAndActiveTrue(id)
                                .orElseThrow(() -> new EnrollmentNotFoundException(
                                                "Active enrollment not found."));
        }

        private void validateEnrollmentAccess(
                        Enrollment enrollment,
                        Student student,
                        EnrollmentProcess openProcess) {

                if (!enrollment.getStudent()
                                .getId()
                                .equals(student.getId())) {

                        throw new InvalidEnrollmentException(
                                        "The enrollment does not belong to the authenticated student.");
                }

                if (!enrollment.getEnrollmentProcess()
                                .getId()
                                .equals(openProcess.getId())) {

                        throw new InvalidEnrollmentException(
                                        "The enrollment does not belong to the open enrollment process.");
                }
        }

        private void removeUnselectedItems(
                        Enrollment enrollment,
                        Set<UUID> selectedProcessClassIds) {

                List<EnrollmentItem> itemsToRemove = enrollment.getItems()
                                .stream()
                                .filter(item -> !selectedProcessClassIds.contains(
                                                item.getEnrollmentProcessClass().getId()))
                                .toList();

                itemsToRemove.forEach(enrollment::removeItem);
        }

        private List<EnrollmentProcessClass> findNewProcessClasses(
                        Enrollment enrollment,
                        List<EnrollmentProcessClass> selectedProcessClasses) {

                Set<UUID> currentProcessClassIds = enrollment.getItems()
                                .stream()
                                .map(item -> item
                                                .getEnrollmentProcessClass()
                                                .getId())
                                .collect(Collectors.toSet());

                return selectedProcessClasses.stream()
                                .filter(processClass -> !currentProcessClassIds.contains(processClass.getId()))
                                .toList();
        }

        private void validateSelectionIds(List<UUID> ids) {

                if (ids == null || ids.isEmpty()) {
                        throw new InvalidEnrollmentException(
                                        "At least one class must be selected.");
                }

                if (ids.size() > 7) {
                        throw new InvalidEnrollmentException(
                                        "An enrollment cannot contain more than 7 classes.");
                }

                long distinctIds = ids.stream()
                                .distinct()
                                .count();

                if (distinctIds != ids.size()) {
                        throw new InvalidEnrollmentException(
                                        "The same class cannot be selected more than once.");
                }
        }

        private Enrollment prepareEnrollment(
                        Student student,
                        EnrollmentProcess process) {

                return enrollmentRepository
                                .findByStudentAndEnrollmentProcess(student, process)
                                .map(existingEnrollment -> {

                                        if (existingEnrollment.isActive()) {
                                                throw new EnrollmentAlreadyExistsException(
                                                                "The student already has an active enrollment for this process.");
                                        }

                                        existingEnrollment.clearItems();
                                        existingEnrollment.setActive(true);

                                        return existingEnrollment;
                                })
                                .orElseGet(() -> Enrollment.builder()
                                                .student(student)
                                                .enrollmentProcess(process)
                                                .build());
        }

        private List<EnrollmentProcessClass> loadAndValidateProcessClasses(
                        List<UUID> ids,
                        EnrollmentProcess process) {

                List<EnrollmentProcessClass> processClasses = enrollmentProcessClassService.findAllByIds(ids);

                validateProcessClasses(processClasses, process);
                validateDuplicateDisciplines(processClasses);

                return processClasses;
        }

        private void validateProcessClasses(
                        List<EnrollmentProcessClass> processClasses,
                        EnrollmentProcess process) {

                for (EnrollmentProcessClass processClass : processClasses) {

                        if (!processClass.isActive()) {
                                throw new InvalidEnrollmentException(
                                                "One or more selected classes are inactive in the enrollment process.");
                        }

                        if (!processClass.getEnrollmentProcess()
                                        .getId()
                                        .equals(process.getId())) {

                                throw new InvalidEnrollmentException(
                                                "One or more selected classes do not belong to the open enrollment process.");
                        }

                        if (!processClass.getClassGroup().isActive()) {
                                throw new InvalidEnrollmentException(
                                                "One or more selected class groups are inactive.");
                        }
                }
        }

        private void validateDuplicateDisciplines(List<EnrollmentProcessClass> processClasses) {

                long distinctDisciplines = processClasses.stream()
                                .map(processClass -> processClass
                                                .getClassGroup()
                                                .getDiscipline()
                                                .getId())
                                .distinct()
                                .count();

                if (distinctDisciplines != processClasses.size()) {
                        throw new InvalidEnrollmentException(
                                        "Only one class group per discipline can be selected.");
                }
        }

        private void validateCapacity(List<EnrollmentProcessClass> processClasses) {

                for (EnrollmentProcessClass processClass : processClasses) {

                        ClassGroup classGroup = processClass.getClassGroup();

                        if (Boolean.TRUE.equals(
                                        classGroup.getAllowOversubscription())) {
                                continue;
                        }

                        long occupiedVacancies = enrollmentItemRepository
                                        .countByEnrollmentProcessClassAndEnrollmentActiveTrue(
                                                        processClass);

                        if (occupiedVacancies >= classGroup.getVacancies()) {
                                throw new InvalidEnrollmentException(
                                                "The class group "
                                                                + classGroup.getCode()
                                                                + " has no available vacancies.");
                        }
                }
        }

        private void addItems(
                        Enrollment enrollment,
                        List<EnrollmentProcessClass> processClasses) {

                for (EnrollmentProcessClass processClass : processClasses) {

                        EnrollmentItem item = EnrollmentItem.builder()
                                        .enrollmentProcessClass(processClass)
                                        .build();

                        enrollment.addItem(item);
                }
        }

        private EnrollmentItemResponseDTO toItemResponse(EnrollmentItem item) {

                EnrollmentProcessClass processClass = item.getEnrollmentProcessClass();

                ClassGroup classGroup = processClass.getClassGroup();

                return new EnrollmentItemResponseDTO(
                                item.getId(),
                                processClass.getId(),
                                classGroup.getId(),
                                classGroup.getCode(),
                                classGroup.getName());
        }

        private EnrollmentResponseDTO toResponse(Enrollment enrollment) {

                List<EnrollmentItemResponseDTO> items = enrollment.getItems()
                                .stream()
                                .map(this::toItemResponse)
                                .toList();

                return new EnrollmentResponseDTO(
                                enrollment.getId(),
                                enrollment.getStudent().getId(),
                                enrollment.getEnrollmentProcess().getId(),
                                enrollment.getEnrollmentProcess().getTitle(),
                                items,
                                items.size(),
                                enrollment.isActive(),
                                enrollment.getCreatedAt(),
                                enrollment.getUpdatedAt());
        }

}