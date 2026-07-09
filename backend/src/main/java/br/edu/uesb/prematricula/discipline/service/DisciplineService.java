package br.edu.uesb.prematricula.discipline.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.uesb.prematricula.discipline.exception.DisciplineNotFoundException;
import br.edu.uesb.prematricula.discipline.model.dto.request.DisciplineRequestDTO;
import br.edu.uesb.prematricula.discipline.model.dto.response.DisciplineResponseDTO;
import br.edu.uesb.prematricula.discipline.model.entity.Discipline;
import br.edu.uesb.prematricula.discipline.repository.DisciplineRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DisciplineService {

    @Autowired
    private DisciplineRepository repository;

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

    @Transactional
    public DisciplineResponseDTO update(UUID id, DisciplineRequestDTO dto) {
        Discipline entity = findEntityById(id);

        // Verifica se o código foi alterado e se o novo código já existe
        if (!entity.getCode().equals(dto.code()) && repository.existsByCode(dto.code())) {
            throw new IllegalArgumentException("Já existe uma disciplina com o código informado.");
        }

        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setWorkload(dto.workload());
        entity.setPrerequisites(dto.prerequisites());

        return toResponse(repository.save(entity));
    }

    public List<DisciplineResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DisciplineResponseDTO findById(UUID id) {
        return toResponse(findEntityById(id));
    }

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
                entity.getActive());
    }
}