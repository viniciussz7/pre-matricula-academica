package br.edu.uesb.prematricula.academicperiod.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, UUID> {

}
