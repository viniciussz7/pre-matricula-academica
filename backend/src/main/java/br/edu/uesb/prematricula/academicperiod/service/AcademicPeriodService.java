package br.edu.uesb.prematricula.academicperiod.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uesb.prematricula.academicperiod.exception.AcademicPeriodNotFoundException;
import br.edu.uesb.prematricula.academicperiod.exception.ResourceNotFoundException;
import br.edu.uesb.prematricula.academicperiod.model.dto.request.AcademicPeriodRequestDTO;
import br.edu.uesb.prematricula.academicperiod.model.dto.response.AcademicPeriodResponseDTO;
import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;
import br.edu.uesb.prematricula.academicperiod.repository.AcademicPeriodRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pelo gerenciamento dos períodos letivos do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>Centraliza as regras de negócio relacionadas ao cadastro,
 * atualização, consulta e desativação de períodos letivos,
 * garantindo o cumprimento das regras institucionais.</p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class AcademicPeriodService {

    private final AcademicPeriodRepository repository;

    /**
     * Cadastra um novo período letivo.
     *
     * <p>Realiza as validações de negócio antes da persistência,
     * verificando a existência de códigos duplicados. O novo período
     * é criado com status ativo por padrão.</p>
     *
     * @param dto dados necessários para criação do período letivo,
     *            contendo código, nome, datas de início e fim
     * @return período letivo criado
     * @throws IllegalArgumentException
     *         quando já existir um período com o mesmo código
     */
    @Transactional
    public AcademicPeriodResponseDTO create(AcademicPeriodRequestDTO dto) {
        if (repository.existsByCode(dto.code())) {
            throw new IllegalArgumentException("Já existe um período letivo com o código informado.");
        }
        AcademicPeriod entity = AcademicPeriod.builder()
                .code(dto.code())
                .name(dto.name())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .active(true)
                .build();
        return toResponse(repository.save(entity));
    }

    /**
     * Atualiza os dados de um período letivo existente.
     *
     * <p>Realiza as validações de negócio antes da atualização,
     * verificando a existência de códigos duplicados apenas se o
     * código foi alterado. Atualiza todos os campos do período,
     * inclusive seu status de atividade.</p>
     *
     * @param id identificador do período letivo a ser atualizado
     * @param dto novos dados do período letivo
     * @return período letivo atualizado
     * @throws ResourceNotFoundException
     *         quando o identificador não existir
     * @throws IllegalArgumentException
     *         quando o novo código já existir para outro período
     */
    @Transactional
    public AcademicPeriodResponseDTO update(UUID id, AcademicPeriodRequestDTO dto) {
        AcademicPeriod entity = findEntityById(id);

        if (!entity.getCode().equals(dto.code()) && repository.existsByCode(dto.code())) {
            throw new IllegalArgumentException("Já existe um período letivo com o código informado.");
        }

        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());
        entity.setActive(dto.active());
        return toResponse(repository.save(entity));
    }

    /**
     * Lista todos os períodos letivos cadastrados.
     *
     * @return lista contendo todos os períodos letivos
     */
    public List<AcademicPeriodResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca um período letivo pelo identificador.
     *
     * @param id identificador do período letivo
     * @return período letivo encontrado
     * @throws ResourceNotFoundException
     *         quando o identificador não existir
     */
    public AcademicPeriodResponseDTO findById(UUID id) {
        return toResponse(findEntityById(id));
    }

    /**
     * Desativa um período letivo.
     *
     * <p>A operação realiza exclusão lógica, preservando o histórico
     * de dados do sistema. Apenas marca o período como inativo,
     * sem removê-lo do banco de dados.</p>
     *
     * @param id identificador do período letivo a ser desativado
     * @throws ResourceNotFoundException
     *         quando o identificador não existir
     */
    @Transactional
    public void deactivate(UUID id) {
        AcademicPeriod entity = findEntityById(id);
        entity.setActive(false);
        repository.save(entity);
    }

    private AcademicPeriod findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Período não encontrado com ID: " + id));
    }

    /**
     * Converte uma entidade de período letivo para seu objeto de resposta.
     *
     * <p>Realiza a transformação de dados da entidade JPA para o DTO
     * de resposta, preparando os dados para exposição através da API.</p>
     *
     * @param entity entidade de período letivo a ser convertida
     * @return objeto de resposta com os dados do período letivo
     */
    public AcademicPeriodResponseDTO toResponse(AcademicPeriod entity) {
        return new AcademicPeriodResponseDTO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.isActive());
    }

    /**
     * Busca um período letivo pelo identificador, retornando a entidade.
     *
     * <p>Diferentemente de {@link #findById(UUID)}, este método retorna
     * a entidade JPA diretamente, útil para operações internas que
     * necessitam da entidade completa.</p>
     *
     * @param id identificador do período letivo
     * @return entidade de período letivo encontrada
     * @throws AcademicPeriodNotFoundException
     *         quando o período não for encontrado
     */
    public AcademicPeriod getAcademicPeriod(UUID id) {

        return repository.findById(id)
                .orElseThrow(() -> new AcademicPeriodNotFoundException(
                        "Academic period not found."));
    }

}
