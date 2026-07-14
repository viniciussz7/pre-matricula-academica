package br.edu.uesb.prematricula.discipline.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.uesb.prematricula.discipline.exception.DisciplineNotFoundException;
import br.edu.uesb.prematricula.discipline.model.dto.request.DisciplineRequestDTO;
import br.edu.uesb.prematricula.discipline.model.dto.response.DisciplineResponseDTO;
import br.edu.uesb.prematricula.discipline.model.entity.Discipline;
import br.edu.uesb.prematricula.discipline.repository.DisciplineRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo gerenciamento de disciplinas do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas ao cadastro,
 * atualização, consulta e desativação de disciplinas,
 * garantindo a integridade e consistência dos dados curriculares.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class DisciplineService {

    private final DisciplineRepository repository;

    /**
     * Cadastra uma nova disciplina.
     *
     * <p>
     * Realiza as validações de negócio antes da persistência,
     * verificando a existência de códigos duplicados. A nova disciplina
     * é criada com status ativo por padrão.
     * </p>
     *
     * @param dto dados necessários para criação da disciplina,
     *            contendo código, nome, carga horária e pré-requisitos
     * @return disciplina criada
     * @throws IllegalArgumentException
     *                                  quando já existir uma disciplina com o mesmo
     *                                  código
     */
    @Transactional
    public DisciplineResponseDTO create(DisciplineRequestDTO dto) {
        if (repository.existsByCode(dto.code())) {
            throw new IllegalArgumentException("Já existe uma disciplina com o código informado.");
        }

        Discipline entity = Discipline.builder()
                .code(dto.code())
                .name(dto.name())
                .workload(dto.workload())
                .prerequisites(dto.prerequisites())
                .active(true)
                .build();

        return toResponse(repository.save(entity));
    }

    /**
     * Atualiza os dados de uma disciplina existente.
     *
     * <p>
     * Realiza as validações de negócio antes da atualização,
     * verificando a existência de códigos duplicados apenas se o
     * código foi alterado. Atualiza todos os campos da disciplina,
     * inclusive seu status de atividade.
     * </p>
     *
     * @param id  identificador da disciplina a ser atualizada
     * @param dto novos dados da disciplina
     * @return disciplina atualizada
     * @throws DisciplineNotFoundException
     *                                     quando o identificador não existir
     * @throws IllegalArgumentException
     *                                     quando o novo código já existir para
     *                                     outra disciplina
     */
    @Transactional
    public DisciplineResponseDTO update(UUID id, DisciplineRequestDTO dto) {
        Discipline entity = findEntityById(id);

        if (!entity.getCode().equals(dto.code()) && repository.existsByCode(dto.code())) {
            throw new IllegalArgumentException("Já existe uma disciplina com o código informado.");
        }

        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setWorkload(dto.workload());
        entity.setPrerequisites(dto.prerequisites());
        entity.setActive(dto.active());

        return toResponse(repository.save(entity));
    }

    /**
     * Lista todas as disciplinas cadastradas.
     *
     * @return lista contendo todas as disciplinas
     */
    public List<DisciplineResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma disciplina pelo identificador.
     *
     * @param id identificador da disciplina
     * @return disciplina encontrada
     * @throws DisciplineNotFoundException
     *                                     quando o identificador não existir
     */
    public DisciplineResponseDTO findById(UUID id) {
        return toResponse(findEntityById(id));
    }

    /**
     * Desativa uma disciplina.
     *
     * <p>
     * A operação realiza exclusão lógica, preservando o histórico
     * de dados do sistema. Apenas marca a disciplina como inativa,
     * sem removê-la do banco de dados.
     * </p>
     *
     * @param id identificador da disciplina a ser desativada
     * @throws DisciplineNotFoundException
     *                                     quando o identificador não existir
     */
    @Transactional
    public void deactivate(UUID id) {
        Discipline entity = findEntityById(id);
        entity.setActive(false);
        repository.save(entity);
    }

    private Discipline findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DisciplineNotFoundException("Disciplina não encontrada com o ID: " + id));
    }

    private DisciplineResponseDTO toResponse(Discipline entity) {
        return new DisciplineResponseDTO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getWorkload(),
                entity.getPrerequisites(),
                entity.isActive());
    }
}