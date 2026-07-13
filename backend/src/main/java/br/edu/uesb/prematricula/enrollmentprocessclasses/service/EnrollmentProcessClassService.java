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

@Service
@RequiredArgsConstructor
public class EnrollmentProcessClassService {

        private final EnrollmentProcessClassRepository repository;

        private final EnrollmentProcessService enrollmentProcessService;

        private final ClassGroupService classGroupService;

        @Transactional
        public EnrollmentProcessClassResponseDTO create(
                        CreateEnrollmentProcessClassRequestDTO dto) {

                EnrollmentProcess process = enrollmentProcessService.getEnrollmentProcess(
                                dto.enrollmentProcessId());

                ClassGroup classGroup = classGroupService.getClassGroup(dto.classGroupId());

                validateProcess(process);

                validateClassGroup(process, classGroup);

                EnrollmentProcessClass entity = EnrollmentProcessClass.builder()
                                .enrollmentProcess(process)
                                .classGroup(classGroup)
                                .build();

                EnrollmentProcessClass saved = repository.save(entity);

                return toResponse(saved);
        }

        public List<EnrollmentProcessClassResponseDTO> findByProcess(
                        UUID processId) {

                EnrollmentProcess process = enrollmentProcessService.getEnrollmentProcess(processId);

                return repository
                                .findByEnrollmentProcessAndActiveTrue(process)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional
        public void deactivate(UUID id) {

                EnrollmentProcessClass entity = get(id);

                validateDeactivate(entity);

                entity.setActive(false);

                repository.save(entity);
        }

        public List<EnrollmentProcessClassResponseDTO> findOpenProcessClasses() {

                EnrollmentProcess process = enrollmentProcessService.getOpenProcess();

                return repository.findByEnrollmentProcessAndActiveTrue(process)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        public EnrollmentProcessClass get(UUID id) {

                return repository.findById(id)
                                .orElseThrow(() -> new EnrollmentProcessClassNotFoundException(
                                                "Enrollment process class not found."));
        }

        public List<EnrollmentProcessClass> findAllByIds(List<UUID> ids) {

                List<EnrollmentProcessClass> classes = repository.findAllById(ids);

                if (classes.size() != ids.size()) {
                        throw new EnrollmentProcessClassNotFoundException(
                                        "One or more enrollment process classes were not found.");
                }

                return classes;
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