package br.edu.uesb.prematricula.verificationtoken.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uesb.prematricula.verificationtoken.model.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByTokenAndUsedFalse(String token);

    boolean existsByTokenAndUsedFalse(String token);
    
}
