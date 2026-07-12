package br.edu.uesb.prematricula.classgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@RequiredArgsConstructor
public class ClassGroupService {

    private ClassGroupRepository repository;
    private DisciplineRepository disciplineRepository;
    private AcademicPeriodRepository academicPeriodRepository;

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

    public List<ClassGroupResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClassGroupResponseDTO findById(UUID id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public void deactivate(UUID id) {
        ClassGroup entity = findEntityById(id);
        entity.setActive(false);
        repository.save(entity);
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
