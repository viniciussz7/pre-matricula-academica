package br.edu.uesb.prematricula.enrollmentprocess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrollmentProcessService {

    private final EnrollmentProcessRepository enrollmentProcessRepository;
    private final AcademicPeriodService academicPeriodService;

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

    public List<EnrollmentProcessResponseDTO> findAll() {

        return enrollmentProcessRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

    }

    public EnrollmentProcessResponseDTO findById(UUID id) {

        EnrollmentProcess process = getEnrollmentProcess(id);

        return toResponse(process);

    }

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

    @Transactional
    public void deactivate(UUID id) {

        EnrollmentProcess process = getEnrollmentProcess(id);

        validateDeactivate(process);

        process.setActive(false);

        enrollmentProcessRepository.save(process);
    }

    public EnrollmentProcessResponseDTO findOpenProcess() {

        EnrollmentProcess process = enrollmentProcessRepository
                .findByActiveTrue()
                .stream()
                .filter(EnrollmentProcess::isOpen)
                .findFirst()
                .orElseThrow(() -> new EnrollmentProcessNotFoundException(
                        "No open enrollment process found."));

        return toResponse(process);
    }

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