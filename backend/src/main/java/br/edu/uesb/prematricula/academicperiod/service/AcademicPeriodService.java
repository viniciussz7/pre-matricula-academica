package br.edu.uesb.prematricula.academicperiod.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.academicperiod.model.dto.request.AcademicPeriodRequestDTO;
import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;
import br.edu.uesb.prematricula.academicperiod.repository.AcademicPeriodRepository;
import jakarta.transaction.Transactional;

@Service
public class AcademicPeriodService {

    @Autowired
    private AcademicPeriodRepository repository;

    @Transactional
    public AcademicPeriod create(AcademicPeriodRequestDTO dto) {
        AcademicPeriod academicPeriod = AcademicPeriod.builder()
                .code(dto.code())
                .name(dto.name())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .active(true)
                .build();

        return repository.save(academicPeriod);
    }

    @Transactional
    public AcademicPeriod update(UUID id, AcademicPeriodRequestDTO dto) {
        AcademicPeriod period = findById(id);
        period.setCode(dto.code());
        period.setName(dto.name());
        period.setStartDate(dto.startDate());
        period.setEndDate(dto.endDate());
        return repository.save(period);
    }

    public List<AcademicPeriod> findAll() {
        return repository.findAll();
    }

    public AcademicPeriod findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Período não encontrado com ID: " + id));
    }

    @Transactional
    public void deactivate(UUID id) {
        AcademicPeriod period = findById(id);
        period.setActive(false);
        repository.save(period);
    }
}
