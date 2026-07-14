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

/**
 * Serviço responsável pelo gerenciamento de tokens de verificação do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas à geração, validação
 * e rastreamento de tokens de primeiro acesso, garantindo a segurança
 * e integridade do processo de autenticação inicial.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
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

    /**
     * Cria um token de primeiro acesso para um usuário.
     *
     * <p>
     * Gera um token numérico único, verifica duplicatas no banco de dados
     * e define o tempo de expiração baseado na configuração do sistema.
     * </p>
     *
     * @param user usuário para o qual o token será criado
     * @return token de primeiro acesso gerado
     */
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

    /**
     * Valida um token de verificação.
     *
     * <p>
     * Realiza múltiplas verificações de segurança: existência do token,
     * status de uso (se já foi utilizado) e validade temporal. Todos os
     * validações geram exceções específicas para diferentes cenários.
     * </p>
     *
     * @param token código do token a ser validado
     * @return token validado
     * @throws InvalidVerificationTokenException
     *                                           quando o token não existir, já
     *                                           tiver sido utilizado,
     *                                           ou tiver expirado
     */
    public VerificationToken validateToken(String token) {

        // token inexistente
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(
                        () -> new InvalidVerificationTokenException("Invalid verification token: token not found"));

        // token ja foi usado
        if (verificationToken.isUsed()) {
            throw new InvalidVerificationTokenException("Invalid verification token: token already used");
        }

        // token expirado
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidVerificationTokenException("Invalid verification token: token expired");
        }

        return verificationToken;

    }

    /**
     * Marca um token como utilizado.
     *
     * <p>
     * Atualiza o status do token no banco de dados, impedindo seu
     * reuso em operações futuras.
     * </p>
     *
     * @param verificationToken token a ser marcado como utilizado
     */
    public void markAsUsed(VerificationToken verificationToken) {
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

}
