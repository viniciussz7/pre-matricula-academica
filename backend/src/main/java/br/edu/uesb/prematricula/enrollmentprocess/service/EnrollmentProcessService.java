package br.edu.uesb.prematricula.enrollmentprocess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;
import br.edu.uesb.prematricula.academicperiod.service.AcademicPeriodService;
import br.edu.uesb.prematricula.enrollmentprocess.exception.EnrollmentProcessAlreadyExistsException;
import br.edu.uesb.prematricula.enrollmentprocess.exception.EnrollmentProcessNotFoundException;
import br.edu.uesb.prematricula.enrollmentprocess.exception.InvalidEnrollmentProcessException;
import br.edu.uesb.prematricula.enrollmentprocess.model.dto.request.CreateEnrollmentProcessRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocess.model.dto.request.UpdateEnrollmentProcessRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocess.model.dto.response.EnrollmentProcessResponseDTO;
import br.edu.uesb.prematricula.enrollmentprocess.model.entity.EnrollmentProcess;
import br.edu.uesb.prematricula.enrollmentprocess.model.enums.EnrollmentProcessStatus;
import br.edu.uesb.prematricula.enrollmentprocess.repository.EnrollmentProcessRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pelo gerenciamento de processos de matrícula do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas ao cadastro,
 * atualização, consulta e desativação de processos de matrícula,
 * controlando períodos de inscrição e validando períodos acadêmicos associados.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class EnrollmentProcessService {

    private final EnrollmentProcessRepository enrollmentProcessRepository;
    private final AcademicPeriodService academicPeriodService;

    /**
     * Cadastra um novo processo de matrícula.
     *
     * <p>
     * Realiza as validações de negócio: verifica existência de processo aberto,
     * valida datas (não podem estar no passado, start deve ser antes de end),
     * e valida o período acadêmico associado (deve estar ativo e sem processo
     * existente).
     * </p>
     *
     * @param dto dados necessários para criação do processo,
     *            contendo título, período acadêmico e datas de início/fim
     * @return processo de matrícula criado
     * @throws EnrollmentProcessAlreadyExistsException
     *                                                 quando já existir processo
     *                                                 aberto ou processo para o
     *                                                 período acadêmico
     * @throws InvalidEnrollmentProcessException
     *                                                 quando datas forem inválidas
     *                                                 ou período acadêmico inativo
     */
    @Transactional
    public EnrollmentProcessResponseDTO create(CreateEnrollmentProcessRequestDTO dto) {

        AcademicPeriod academicPeriod = academicPeriodService.getAcademicPeriod(dto.academicPeriodId());

        validateDates(dto.startDate(), dto.endDate());
        validateAcademicPeriod(academicPeriod);
        validateNoOpenProcess();

        EnrollmentProcess enrollmentProcess = EnrollmentProcess.builder()
                .title(dto.title())
                .academicPeriod(academicPeriod)
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .build();

        EnrollmentProcess savedProcess = enrollmentProcessRepository.save(enrollmentProcess);

        return toResponse(savedProcess);
    }

    /**
     * Lista todos os processos de matrícula cadastrados.
     *
     * @return lista contendo todos os processos de matrícula
     */
    public List<EnrollmentProcessResponseDTO> findAll() {

        return enrollmentProcessRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

    }

    /**
     * Busca um processo de matrícula pelo identificador.
     *
     * @param id identificador do processo de matrícula
     * @return processo de matrícula encontrado
     * @throws EnrollmentProcessNotFoundException
     *                                            quando o identificador não existir
     */
    public EnrollmentProcessResponseDTO findById(UUID id) {

        EnrollmentProcess process = getEnrollmentProcess(id);

        return toResponse(process);

    }

    /**
     * Atualiza os dados de um processo de matrícula existente.
     *
     * <p>
     * Realiza validações de negócio antes da atualização:
     * processo deve estar no status NOT_STARTED (não iniciado),
     * datas devem ser válidas (não no passado).
     * </p>
     *
     * @param id  identificador do processo a ser atualizado
     * @param dto novos dados do processo
     * @return processo atualizado
     * @throws EnrollmentProcessNotFoundException
     *                                            quando o identificador não existir
     * @throws InvalidEnrollmentProcessException
     *                                            quando processo já tiver iniciado
     *                                            ou datas forem inválidas
     */
    @Transactional
    public EnrollmentProcessResponseDTO update(
            UUID id,
            UpdateEnrollmentProcessRequestDTO dto) {

        EnrollmentProcess process = getEnrollmentProcess(id);

        validateUpdate(process);

        validateDates(dto.startDate(), dto.endDate());

        process.setTitle(dto.title());
        process.setStartDate(dto.startDate());
        process.setEndDate(dto.endDate());
        process.setActive(dto.active());

        EnrollmentProcess saved = enrollmentProcessRepository.save(process);

        return toResponse(saved);
    }

    /**
     * Desativa um processo de matrícula.
     *
     * <p>
     * A operação realiza exclusão lógica, preservando o histórico
     * de dados do sistema. Apenas marca o processo como inativo.
     * </p>
     *
     * @param id identificador do processo a ser desativado
     * @throws EnrollmentProcessNotFoundException
     *                                            quando o identificador não existir
     * @throws InvalidEnrollmentProcessException
     *                                            quando o processo já estiver
     *                                            inativo
     */
    @Transactional
    public void deactivate(UUID id) {

        EnrollmentProcess process = getEnrollmentProcess(id);

        validateDeactivate(process);

        process.setActive(false);

        enrollmentProcessRepository.save(process);
    }

    /**
     * Busca o processo de matrícula aberto no momento.
     *
     * <p>
     * Um processo está aberto quando está ativo e suas datas
     * {@code start <= now <= end} o permitirem.
     * </p>
     *
     * @return processo de matrícula aberto
     * @throws EnrollmentProcessNotFoundException
     *                                            quando nenhum processo aberto for
     *                                            encontrado
     */
    public EnrollmentProcess getOpenProcess() {

        return enrollmentProcessRepository
                .findByActiveTrue()
                .stream()
                .filter(EnrollmentProcess::isOpen)
                .findFirst()
                .orElseThrow(() -> new EnrollmentProcessNotFoundException(
                        "No open enrollment process found."));
    }

    /**
     * Busca o processo de matrícula aberto e retorna seu DTO de resposta.
     *
     * @return DTO do processo de matrícula aberto
     * @throws EnrollmentProcessNotFoundException
     *                                            quando nenhum processo aberto for
     *                                            encontrado
     */
    public EnrollmentProcessResponseDTO findOpenProcess() {

        return toResponse(getOpenProcess());
    }

    /**
     * Busca um processo de matrícula pelo identificador, retornando a entidade.
     *
     * <p>
     * Diferentemente de {@link #findById(UUID)}, este método retorna
     * a entidade JPA diretamente, útil para operações internas que
     * necessitam da entidade completa.
     * </p>
     *
     * @param id identificador do processo de matrícula
     * @return entidade de processo encontrada
     * @throws EnrollmentProcessNotFoundException
     *                                            quando o identificador não existir
     */
    public EnrollmentProcess getEnrollmentProcess(UUID id) {

        return enrollmentProcessRepository.findById(id)
                .orElseThrow(() -> new EnrollmentProcessNotFoundException(
                        "Enrollment process not found."));

    }

    private void validateDeactivate(
            EnrollmentProcess process) {

        if (!process.isActive()) {
            throw new InvalidEnrollmentProcessException(
                    "Enrollment process is already inactive.");
        }

        // if (hasEnrollments()) ...

    }

    private void validateNoOpenProcess() {

        boolean hasOpenProcess = enrollmentProcessRepository
                .findByActiveTrue()
                .stream()
                .anyMatch(EnrollmentProcess::isOpen);

        if (hasOpenProcess) {
            throw new EnrollmentProcessAlreadyExistsException(
                    "There is already an open enrollment process.");
        }
    }

    private void validateDates(LocalDateTime start,
            LocalDateTime end) {

        if (!start.isBefore(end)) {
            throw new InvalidEnrollmentProcessException(
                    "Start date must be before end date.");
        }

        if (end.isBefore(LocalDateTime.now())) {
            throw new InvalidEnrollmentProcessException(
                    "End date cannot be in the past.");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new InvalidEnrollmentProcessException(
                    "Start date cannot be in the past.");
        }

    }

    private void validateUpdate(
            EnrollmentProcess process) {

        if (process.getStatus() != EnrollmentProcessStatus.NOT_STARTED) {
            throw new InvalidEnrollmentProcessException(
                    "Only enrollment processes that have not started can be updated.");
        }
    }

    private void validateAcademicPeriod(AcademicPeriod academicPeriod) {

        if (enrollmentProcessRepository.existsByAcademicPeriod(academicPeriod)) {
            throw new EnrollmentProcessAlreadyExistsException(
                    "An enrollment process already exists for this academic period.");
        }

        if (!academicPeriod.isActive()) {
            throw new InvalidEnrollmentProcessException(
                    "Academic period is inactive.");
        }

    }

    private EnrollmentProcessResponseDTO toResponse(
            EnrollmentProcess process) {

        return new EnrollmentProcessResponseDTO(
                process.getId(),
                process.getTitle(),
                process.getAcademicPeriod().getId(),
                process.getAcademicPeriod().getCode(),
                process.getAcademicPeriod().getName(),
                process.getStartDate(),
                process.getEndDate(),
                process.getStatus(),
                process.isActive());
    }

}