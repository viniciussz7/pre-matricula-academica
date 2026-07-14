package br.edu.uesb.prematricula.enrollmentprocessclasses.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uesb.prematricula.classgroup.model.entity.ClassGroup;
import br.edu.uesb.prematricula.classgroup.service.ClassGroupService;
import br.edu.uesb.prematricula.enrollmentprocess.exception.InvalidEnrollmentProcessException;
import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;
import br.edu.uesb.prematricula.enrollmentprocess.model.enums.EnrollmentProcessStatus;
import br.edu.uesb.prematricula.enrollmentprocess.service.EnrollmentProcessService;
import br.edu.uesb.prematricula.enrollmentprocessclasses.exception.EnrollmentProcessClassAlreadyExistsException;
import br.edu.uesb.prematricula.enrollmentprocessclasses.exception.EnrollmentProcessClassNotFoundException;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.request.CreateEnrollmentProcessClassRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.response.EnrollmentProcessClassResponseDTO;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.entity.EnrollmentProcessClass;
import br.edu.uesb.prematricula.enrollmentprocessclasses.repository.EnrollmentProcessClassRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pelo gerenciamento de turmas em processos de matrícula
 * no sistema de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas à adição de turmas aos
 * processos de matrícula, validando compatibilidade entre turmas e períodos
 * acadêmicos, além de gerenciar desativações e consultas de turmas disponíveis.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class EnrollmentProcessClassService {

        private final EnrollmentProcessClassRepository repository;

        private final EnrollmentProcessService enrollmentProcessService;

        private final ClassGroupService classGroupService;

        /**
         * Adiciona uma turma a um processo de matrícula.
         *
         * <p>
         * Realiza validações: processo deve estar ativo e não iniciado,
         * turma deve estar ativa e pertencer ao mesmo período acadêmico do processo.
         * Se a associação já existe e está inativa, reativa-a.
         * </p>
         *
         * @param dto dados contendo ID do processo e ID da turma
         * @return turma do processo criada ou reativada
         * 
         * @throws EnrollmentProcessClassAlreadyExistsException
         *                                                      quando a turma já
         *                                                      estiver ativa neste
         *                                                      processo
         */
        @Transactional
        public EnrollmentProcessClassResponseDTO create(
                        CreateEnrollmentProcessClassRequestDTO dto) {

                EnrollmentProcess process = enrollmentProcessService.getEnrollmentProcess(
                                dto.enrollmentProcessId());

                ClassGroup classGroup = classGroupService.getClassGroup(
                                dto.classGroupId());

                validateProcess(process);
                validateClassGroupPeriod(process, classGroup);

                EnrollmentProcessClass entity = repository
                                .findByEnrollmentProcessAndClassGroup(
                                                process,
                                                classGroup)
                                .map(existing -> {

                                        if (existing.isActive()) {
                                                throw new EnrollmentProcessClassAlreadyExistsException(
                                                                "Class group already belongs to this enrollment process.");
                                        }

                                        existing.setActive(true);

                                        return existing;
                                })
                                .orElseGet(() -> EnrollmentProcessClass.builder()
                                                .enrollmentProcess(process)
                                                .classGroup(classGroup)
                                                .build());

                EnrollmentProcessClass saved = repository.save(entity);

                return toResponse(saved);
        }

        /**
         * Lista todas as turmas ativas de um processo de matrícula.
         *
         * @param processId identificador do processo de matrícula
         * @return lista de turmas ativas no processo
         */
        public List<EnrollmentProcessClassResponseDTO> findByProcess(
                        UUID processId) {

                EnrollmentProcess process = enrollmentProcessService.getEnrollmentProcess(processId);

                return repository
                                .findByEnrollmentProcessAndActiveTrue(process)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        /**
         * Desativa uma turma de um processo de matrícula.
         *
         * <p>
         * A operação realiza exclusão lógica, preservando o histórico
         * de dados do sistema.
         * </p>
         *
         * @param id identificador da turma do processo a ser desativada
         *
         * 
         */
        @Transactional
        public void deactivate(UUID id) {

                EnrollmentProcessClass entity = get(id);

                validateDeactivate(entity);

                entity.setActive(false);

                repository.save(entity);
        }

        /**
         * Lista todas as turmas ativas do processo de matrícula aberto.
         *
         * @return lista de turmas ativas no processo aberto
         * 
         */
        public List<EnrollmentProcessClassResponseDTO> findOpenProcessClasses() {

                EnrollmentProcess process = enrollmentProcessService.getOpenProcess();

                return repository.findByEnrollmentProcessAndActiveTrue(process)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        /**
         * Busca uma turma do processo pelo identificador, retornando a entidade.
         *
         * @param id identificador da turma do processo
         * @return entidade de turma do processo encontrada
         * @throws EnrollmentProcessClassNotFoundException
         *                                                 quando o identificador não
         *                                                 existir
         */
        public EnrollmentProcessClass get(UUID id) {

                return repository.findById(id)
                                .orElseThrow(() -> new EnrollmentProcessClassNotFoundException(
                                                "Enrollment process class not found."));
        }

        /**
         * Busca múltiplas turmas do processo pelos identificadores.
         *
         * <p>
         * Valida que todos os IDs fornecidos foram encontrados,
         * lançando exceção se algum ID não existir.
         * </p>
         *
         * @param ids lista de identificadores de turmas do processo
         * @return lista de entidades encontradas na mesma ordem fornecida
         * @throws EnrollmentProcessClassNotFoundException
         *                                                 quando algum identificador
         *                                                 não existir
         */
        public List<EnrollmentProcessClass> findAllByIds(List<UUID> ids) {

                List<EnrollmentProcessClass> classes = repository.findAllById(ids);

                if (classes.size() != ids.size()) {
                        throw new EnrollmentProcessClassNotFoundException(
                                        "One or more enrollment process classes were not found.");
                }

                return classes;
        }

        private void validateClassGroupPeriod(
                        EnrollmentProcess process,
                        ClassGroup classGroup) {

                if (!classGroup.isActive()) {
                        throw new InvalidEnrollmentProcessException(
                                        "Class group is inactive.");
                }

                if (!process.getAcademicPeriod()
                                .getId()
                                .equals(classGroup.getAcademicPeriod().getId())) {

                        throw new InvalidEnrollmentProcessException(
                                        "Class group belongs to another academic period.");
                }
        }

        private void validateProcess(
                        EnrollmentProcess process) {

                if (!process.isActive()) {
                        throw new InvalidEnrollmentProcessException(
                                        "Enrollment process is inactive.");
                }

                if (process.getStatus() != EnrollmentProcessStatus.NOT_STARTED) {

                        throw new InvalidEnrollmentProcessException(
                                        "Only enrollment processes that have not started can be modified.");
                }
        }

        private void validateClassGroup(
                        EnrollmentProcess process,
                        ClassGroup classGroup) {

                if (!classGroup.isActive()) {
                        throw new InvalidEnrollmentProcessException(
                                        "Class group is inactive.");
                }

                if (!process.getAcademicPeriod()
                                .getId()
                                .equals(classGroup.getAcademicPeriod().getId())) {

                        throw new InvalidEnrollmentProcessException(
                                        "Class group belongs to another academic period.");
                }

                if (repository.existsByEnrollmentProcessAndClassGroup(
                                process,
                                classGroup)) {

                        throw new EnrollmentProcessClassAlreadyExistsException(
                                        "Class group already belongs to this enrollment process.");
                }
        }

        private void validateDeactivate(
                        EnrollmentProcessClass entity) {

                if (!entity.isActive()) {
                        throw new InvalidEnrollmentProcessException(
                                        "Enrollment process class is already inactive.");
                }
        }

        private EnrollmentProcessClassResponseDTO toResponse(
                        EnrollmentProcessClass entity) {

                return new EnrollmentProcessClassResponseDTO(
                                entity.getId(),
                                entity.getEnrollmentProcess().getId(),
                                entity.getEnrollmentProcess().getTitle(),
                                entity.getClassGroup().getId(),
                                entity.getClassGroup().getCode(),
                                entity.getClassGroup().getName(),
                                entity.isActive());
        }

}