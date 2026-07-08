package br.edu.uesb.prematricula.academicperiod.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.academicperiod.model.dto.request.AcademicPeriodRequestDTO;
import br.edu.uesb.prematricula.academicperiod.model.dto.response.AcademicPeriodResponseDTO;
import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;
import br.edu.uesb.prematricula.academicperiod.repository.AcademicPeriodRepository;
import jakarta.transaction.Transactional;

@Service
public class AcademicPeriodService {

    @Autowired
    private AcademicPeriodRepository repository;

    @Transactional
    public AcademicPeriodResponseDTO create(AcademicPeriodRequestDTO dto) {
        AcademicPeriod entity = AcademicPeriod.builder()
                .code(dto.code())
                .name(dto.name())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .active(true)
                .build();
        return toResponse(repository.save(entity));
    }

    @Transactional
    public AcademicPeriodResponseDTO update(UUID id, AcademicPeriodRequestDTO dto) {
        AcademicPeriod entity = findEntityById(id);
        entity.setCode(dto.code());
        entity.setName(dto.name());
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());
        return toResponse(repository.save(entity));
    }

    public List<AcademicPeriodResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AcademicPeriodResponseDTO findById(UUID id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public void deactivate(UUID id) {
        AcademicPeriod entity = findEntityById(id);
        entity.setActive(false);
        repository.save(entity);
    }

    private AcademicPeriod findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Período não encontrado com ID: " + id));
    }

    public AcademicPeriodResponseDTO toResponse(AcademicPeriod entity) {
        return new AcademicPeriodResponseDTO(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getActive());
    }

}
