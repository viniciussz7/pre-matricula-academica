package br.edu.uesb.prematricula.discipline.repository;

import br.edu.uesb.prematricula.discipline.model.entity.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, UUID> {
    boolean existsByCode(String code);
}
