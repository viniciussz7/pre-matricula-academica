package br.edu.uesb.prematricula.discipline.repository;

import br.edu.uesb.prematricula.discipline.model.entity.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DisciplineRepository extends JpaRepository<Discipline, UUID> {
    boolean existsByCode(String code);
}
