package br.edu.uesb.prematricula.classgroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;
import br.edu.uesb.prematricula.academicperiod.repository.AcademicPeriodRepository;
import br.edu.uesb.prematricula.classgroup.exception.ClassGroupNotFoundException;
import br.edu.uesb.prematricula.classgroup.model.dto.request.ClassGroupRequestDTO;
import br.edu.uesb.prematricula.classgroup.model.dto.response.ClassGroupResponseDTO;
import br.edu.uesb.prematricula.classgroup.model.entity.ClassGroup;
import br.edu.uesb.prematricula.classgroup.repository.ClassGroupRepository;
import br.edu.uesb.prematricula.discipline.model.entity.Discipline;
import br.edu.uesb.prematricula.discipline.repository.DisciplineRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de turmas do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas ao cadastro,
 * atualização, consulta e desativação de turmas,
 * mantendo a integridade entre disciplinas e períodos acadêmicos.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class ClassGroupService {

    private final ClassGroupRepository repository;
    private final DisciplineRepository disciplineRepository;
    private final AcademicPeriodRepository academicPeriodRepository;

    /**
     * Cadastra uma nova turma.
     *
     * <p>
     * Realiza as validações de negócio antes da persistência,
     * verificando a unicidade da combinação código e período acadêmico.
     * Valida existência da disciplina e do período acadêmico informados.
     * </p>
     *
     * @param dto dados necessários para criação da turma,
     *            contendo código, nome, disciplina, período acadêmico e vagas
     * @return turma criada
     * @throws IllegalArgumentException
     *                                  quando já existir turma com mesmo código no
     *                                  período
     * @throws RuntimeException
     *                                  quando disciplina ou período acadêmico não
     *                                  existir
     */
    @Transactional
    public ClassGroupResponseDTO create(ClassGroupRequestDTO dto) {
        validateUniqueConstraint(dto.code(), dto.academicPeriodId());

        Discipline discipline = disciplineRepository.findById(dto.disciplineId())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));

        AcademicPeriod academicPeriod = academicPeriodRepository.findById(dto.academicPeriodId())
                .orElseThrow(() -> new RuntimeException("Período letivo não encontrado."));

        ClassGroup entity = ClassGroup.builder()
                .code(dto.code())
                .name(dto.name())
                .discipline(discipline)
                .academicPeriod(academicPeriod)
                .vacancies(dto.vacancies())
                .allowOversubscription(dto.allowOversubscription())
                .active(true)
                .build();

        return toResponse(repository.save(entity));
    }

    /**
     * Atualiza os dados de uma turma existente.
     *
     * <p>
     * Realiza as validações de negócio antes da atualização,
     * verificando a unicidade da combinação código e período acadêmico
     * apenas se essas informações foram alteradas. Valida existência
     * da disciplina e período acadêmico informados.
     * </p>
     *
     * @param id  identificador da turma a ser atualizada
     * @param dto novos dados da turma
     * @return turma atualizada
     * @throws ClassGroupNotFoundException
     *                                     quando o identificador não existir
     * @throws IllegalArgumentException
     *                                     quando novo código já existir para o
     *                                     período
     * @throws RuntimeException
     *                                     quando disciplina ou período acadêmico
     *                                     não existir
     */
    @Transactional
    public ClassGroupResponseDTO update(UUID id, ClassGroupRequestDTO dto) {
        ClassGroup entity = findEntityById(id);

        boolean changedCodeOrPeriod = !entity.getCode().equals(dto.code()) ||
                !entity.getAcademicPeriod().getId().equals(dto.academicPeriodId());

        if (changedCodeOrPeriod) {
            validateUniqueConstraint(dto.code(), dto.academicPeriodId());
        }

        Discipline discipline = disciplineRepository.findById(dto.disciplineId())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada."));

        AcademicPeriod academicPeriod = academicPeriodRepository.findById(dto.academicPeriodId())
                .orElseThrow(() -> new RuntimeException("Período letivo não encontrado."));

        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setDiscipline(discipline);
        entity.setAcademicPeriod(academicPeriod);
        entity.setVacancies(dto.vacancies());
        entity.setAllowOversubscription(dto.allowOversubscription());
        entity.setActive(dto.active());

        return toResponse(repository.save(entity));
    }

    /**
     * Lista todas as turmas cadastradas.
     *
     * @return lista contendo todas as turmas
     */
    public List<ClassGroupResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma turma pelo identificador.
     *
     * @param id identificador da turma
     * @return turma encontrada
     * @throws ClassGroupNotFoundException
     *                                     quando o identificador não existir
     */
    public ClassGroupResponseDTO findById(UUID id) {
        return toResponse(findEntityById(id));
    }

    /**
     * Desativa uma turma.
     *
     * <p>
     * A operação realiza exclusão lógica, preservando o histórico
     * de dados do sistema. Apenas marca a turma como inativa,
     * sem removê-la do banco de dados.
     * </p>
     *
     * @param id identificador da turma a ser desativada
     * @throws ClassGroupNotFoundException
     *                                     quando o identificador não existir
     */
    @Transactional
    public void deactivate(UUID id) {
        ClassGroup entity = findEntityById(id);
        entity.setActive(false);
        repository.save(entity);
    }

    /**
     * Busca uma turma pelo identificador, retornando a entidade.
     *
     * <p>
     * Diferentemente de {@link #findById(UUID)}, este método retorna
     * a entidade JPA diretamente, útil para operações internas que
     * necessitam da entidade completa.
     * </p>
     *
     * @param id identificador da turma
     * @return entidade de turma encontrada
     * @throws ClassGroupNotFoundException
     *                                     quando o identificador não existir
     */
    public ClassGroup getClassGroup(UUID id) {

        return repository.findById(id)
                .orElseThrow(() -> new ClassGroupNotFoundException(
                        "Class group not found."));
    }

    private ClassGroup findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ClassGroupNotFoundException("Turma não encontrada com o ID: " + id));
    }

    private void validateUniqueConstraint(String code, UUID academicPeriodId) {
        if (repository.existsByCodeAndAcademicPeriodId(code, academicPeriodId)) {
            throw new IllegalArgumentException("Já existe uma turma com este código neste período letivo.");
        }
    }

    private ClassGroupResponseDTO toResponse(ClassGroup entity) {
        return new ClassGroupResponseDTO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDiscipline().getId(),
                entity.getAcademicPeriod().getId(),
                entity.getVacancies(),
                entity.getAllowOversubscription(),
                entity.isActive());
    }
}
