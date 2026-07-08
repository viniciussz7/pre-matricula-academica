package br.edu.uesb.prematricula.verificationtoken.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.verificationtoken.exception.InvalidVerificationTokenException;
import br.edu.uesb.prematricula.verificationtoken.model.entity.VerificationToken;
import br.edu.uesb.prematricula.verificationtoken.model.enums.TokenType;
import br.edu.uesb.prematricula.verificationtoken.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${app.verification-token.expiration}")
    private long expirationMinutes;

    private String generateToken() {
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }

    public VerificationToken createFirstAccessToken(User user) {

        String token;

        do {
            token = generateToken();
        } while (verificationTokenRepository.existsByTokenAndUsedFalse(token));

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .type(TokenType.FIRST_ACCESS)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .user(user)
                .build();

        return verificationTokenRepository.save(verificationToken);
    }

    public VerificationToken validateToken(String token) {
        
        //token inexistente
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token: token not found"));
        
        //token ja foi usado
        if (verificationToken.isUsed()) {
            throw new InvalidVerificationTokenException("Invalid verification token: token already used");
        }

        //token expirado
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVerificationTokenException("Invalid verification token: token expired");
        }

        return verificationToken;

    }

    public void markAsUsed(VerificationToken verificationToken) {
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

}
