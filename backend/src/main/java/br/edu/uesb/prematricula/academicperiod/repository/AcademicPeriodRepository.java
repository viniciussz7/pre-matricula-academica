package br.edu.uesb.prematricula.academicperiod.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;

public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, UUID> {
    boolean existsByCode(String code);
}
