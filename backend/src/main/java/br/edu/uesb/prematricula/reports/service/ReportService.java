package br.edu.uesb.prematricula.reports.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uesb.prematricula.classgroup.model.entity.ClassGroup;
import br.edu.uesb.prematricula.enrollment.model.entity.Enrollment;
import br.edu.uesb.prematricula.enrollment.model.entity.EnrollmentItem;
import br.edu.uesb.prematricula.enrollment.repository.EnrollmentItemRepository;
import br.edu.uesb.prematricula.enrollment.repository.EnrollmentRepository;
import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;
import br.edu.uesb.prematricula.enrollmentprocess.service.EnrollmentProcessService;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.entity.EnrollmentProcessClass;
import br.edu.uesb.prematricula.enrollmentprocessclasses.repository.EnrollmentProcessClassRepository;
import br.edu.uesb.prematricula.enrollmentprocessclasses.service.EnrollmentProcessClassService;
import br.edu.uesb.prematricula.reports.model.dto.response.ClassDemandResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.EnrolledStudentResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.MostDemandedClassResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.ProcessSummaryResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.StudentWithoutEnrollmentResponseDTO;
import br.edu.uesb.prematricula.student.model.entity.Student;
import br.edu.uesb.prematricula.student.repository.StudentRepository;
import br.edu.uesb.prematricula.user.model.entity.User;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela geração de relatórios de matrículas do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as operações de consulta e análise de dados relacionados a
 * processos de matrícula, demanda de turmas, estudantes inscritos e
 * estatísticas
 * agregadas do processo.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class ReportService {

        private final EnrollmentProcessService enrollmentProcessService;

        private final EnrollmentProcessClassRepository enrollmentProcessClassRepository;

        private final EnrollmentItemRepository enrollmentItemRepository;

        private final EnrollmentProcessClassService enrollmentProcessClassService;

        private final EnrollmentRepository enrollmentRepository;

        private final StudentRepository studentRepository;

        /**
         * Gera relatório de demanda por turma de um processo de matrícula.
         *
         * <p>
         * Retorna informações sobre ocupação, vagas disponíveis, percentual de
         * ocupação de cada turma ativa no processo.
         * </p>
         *
         * @param processId identificador do processo de matrícula
         * @return lista com informações de demanda por turma
         */
        @Transactional(readOnly = true)
        public List<ClassDemandResponseDTO> findClassDemand(
                        UUID processId) {

                EnrollmentProcess process = enrollmentProcessService
                                .getEnrollmentProcess(processId);

                return enrollmentProcessClassRepository
                                .findByEnrollmentProcessAndActiveTrue(process)
                                .stream()
                                .map(this::toClassDemandResponse)
                                .toList();
        }

        /**
         * Gera relatório de estudantes inscritos em uma turma do processo.
         *
         * <p>
         * Retorna lista de todos os estudantes com matrículas ativas inscritos
         * na turma, ordenados alfabeticamente por nome.
         * </p>
         *
         * @param processClassId identificador da turma do processo
         * @return lista de estudantes inscritos na turma
         */
        @Transactional(readOnly = true)
        public List<EnrolledStudentResponseDTO> findStudentsByProcessClass(
                        UUID processClassId) {

                EnrollmentProcessClass processClass = enrollmentProcessClassService.get(processClassId);

                return enrollmentItemRepository
                                .findByEnrollmentProcessClassAndEnrollmentActiveTrue(
                                                processClass)
                                .stream()
                                .map(this::toEnrolledStudentResponse)
                                .sorted(
                                                Comparator.comparing(
                                                                EnrolledStudentResponseDTO::studentName,
                                                                String.CASE_INSENSITIVE_ORDER))
                                .toList();
        }

        /**
         * Gera relatório resumido estatístico de um processo de matrícula.
         *
         * <p>
         * Retorna informações agregadas: total de matrículas (ativas e canceladas),
         * total de seleções, média de turmas por matrícula, número de turmas cheias
         * e turmas com sobrecarga, e turma mais procurada.
         * </p>
         *
         * @param processId identificador do processo de matrícula
         * @return resumo estatístico do processo
         */
        @Transactional(readOnly = true)
        public ProcessSummaryResponseDTO findProcessSummary(
                        UUID processId) {

                EnrollmentProcess process = enrollmentProcessService.getEnrollmentProcess(processId);

                List<Enrollment> enrollments = enrollmentRepository.findByEnrollmentProcess(process);

                List<Enrollment> activeEnrollments = enrollments.stream()
                                .filter(Enrollment::isActive)
                                .toList();

                List<EnrollmentProcessClass> processClasses = enrollmentProcessClassRepository
                                .findByEnrollmentProcessAndActiveTrue(process);

                long totalEnrollments = enrollments.size();

                long activeEnrollmentCount = activeEnrollments.size();

                long cancelledEnrollments = totalEnrollments - activeEnrollmentCount;

                long totalSelections = activeEnrollments.stream()
                                .mapToLong(enrollment -> enrollment.getItems().size())
                                .sum();

                double averageClassesPerActiveEnrollment = calculateAverageClasses(
                                totalSelections,
                                activeEnrollmentCount);

                long fullClasses = 0;

                long oversubscribedClasses = 0;

                EnrollmentProcessClass mostDemandedProcessClass = null;

                long highestDemand = 0;

                for (EnrollmentProcessClass processClass : processClasses) {

                        long demand = enrollmentItemRepository
                                        .countByEnrollmentProcessClassAndEnrollmentActiveTrue(
                                                        processClass);

                        int vacancies = processClass.getClassGroup().getVacancies();

                        if (demand >= vacancies) {
                                fullClasses++;
                        }

                        if (demand > vacancies) {
                                oversubscribedClasses++;
                        }

                        if (mostDemandedProcessClass == null
                                        || demand > highestDemand) {

                                mostDemandedProcessClass = processClass;
                                highestDemand = demand;
                        }
                }

                MostDemandedClassResponseDTO mostDemandedClass = toMostDemandedClassResponse(
                                mostDemandedProcessClass,
                                highestDemand);

                return new ProcessSummaryResponseDTO(
                                process.getId(),
                                process.getTitle(),
                                totalEnrollments,
                                activeEnrollmentCount,
                                cancelledEnrollments,
                                processClasses.size(),
                                totalSelections,
                                averageClassesPerActiveEnrollment,
                                fullClasses,
                                oversubscribedClasses,
                                mostDemandedClass);
        }

        /**
         * Gera relatório de estudantes sem matrícula ativa em um processo.
         *
         * <p>
         * Retorna lista de todos os estudantes ativos que não possuem
         * matrícula ativa no processo de matrícula, ordenados por nome.
         * </p>
         *
         * @param processId identificador do processo de matrícula
         * @return lista de estudantes sem matrícula no processo
         */
        @Transactional(readOnly = true)
        public List<StudentWithoutEnrollmentResponseDTO> findStudentsWithoutEnrollment(
                        UUID processId) {

                EnrollmentProcess process = enrollmentProcessService
                                .getEnrollmentProcess(processId);

                Set<UUID> enrolledStudentIds = enrollmentRepository
                                .findByEnrollmentProcessAndActiveTrue(process)
                                .stream()
                                .map(enrollment -> enrollment.getStudent().getId())
                                .collect(Collectors.toSet());

                return studentRepository
                                .findByActiveTrueAndUserActiveTrueOrderByUserFullNameAsc()
                                .stream()
                                .filter(student -> !enrolledStudentIds.contains(student.getId()))
                                .map(this::toStudentWithoutEnrollmentResponse)
                                .toList();
        }

        private ClassDemandResponseDTO toClassDemandResponse(
                        EnrollmentProcessClass processClass) {

                ClassGroup classGroup = processClass.getClassGroup();

                long enrolledStudents = enrollmentItemRepository
                                .countByEnrollmentProcessClassAndEnrollmentActiveTrue(
                                                processClass);

                int vacancies = classGroup.getVacancies();

                int remainingVacancies = vacancies - Math.toIntExact(enrolledStudents);

                double occupancyPercentage = calculateOccupancyPercentage(
                                enrolledStudents,
                                vacancies);

                return new ClassDemandResponseDTO(
                                processClass.getId(),
                                classGroup.getId(),
                                classGroup.getCode(),
                                classGroup.getName(),
                                classGroup.getDiscipline().getCode(),
                                classGroup.getDiscipline().getName(),
                                vacancies,
                                classGroup.getAllowOversubscription(),
                                enrolledStudents,
                                remainingVacancies,
                                occupancyPercentage);
        }

        private EnrolledStudentResponseDTO toEnrolledStudentResponse(
                        EnrollmentItem item) {

                Enrollment enrollment = item.getEnrollment();

                Student student = enrollment.getStudent();

                User user = student.getUser();

                return new EnrolledStudentResponseDTO(
                                item.getId(),
                                enrollment.getId(),
                                student.getId(),
                                student.getRegistrationNumber(),
                                user.getFullName(),
                                user.getEmail(),
                                item.getCreatedAt());
        }

        private StudentWithoutEnrollmentResponseDTO toStudentWithoutEnrollmentResponse(
                        Student student) {

                User user = student.getUser();

                return new StudentWithoutEnrollmentResponseDTO(
                                student.getId(),
                                user.getId(),
                                student.getRegistrationNumber(),
                                user.getFullName(),
                                user.getEmail(),
                                user.isFirstAccess());
        }

        private double calculateOccupancyPercentage(
                        long enrolledStudents,
                        int vacancies) {

                if (vacancies == 0) {
                        return 0.0;
                }

                double percentage = enrolledStudents * 100.0 / vacancies;

                return Math.round(percentage * 100.0) / 100.0;
        }

        private double calculateAverageClasses(
                        long totalSelections,
                        long activeEnrollments) {

                if (activeEnrollments == 0) {
                        return 0.0;
                }

                double average = (double) totalSelections / activeEnrollments;

                return Math.round(average * 100.0) / 100.0;
        }

        private MostDemandedClassResponseDTO toMostDemandedClassResponse(
                        EnrollmentProcessClass processClass,
                        long enrolledStudents) {

                if (processClass == null) {
                        return null;
                }

                ClassGroup classGroup = processClass.getClassGroup();

                return new MostDemandedClassResponseDTO(
                                processClass.getId(),
                                classGroup.getId(),
                                classGroup.getCode(),
                                classGroup.getName(),
                                enrolledStudents);
        }
}