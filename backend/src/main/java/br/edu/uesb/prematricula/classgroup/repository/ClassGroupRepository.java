package br.edu.uesb.prematricula.classgroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.uesb.prematricula.classgroup.model.entity.ClassGroup;

import java.util.UUID;

@Repository
public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {
    boolean existsByCode(String code);
    boolean existsByCodeAndAcademicPeriodId(String code, UUID academicPeriodId);
}
