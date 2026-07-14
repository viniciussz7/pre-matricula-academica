package br.edu.uesb.prematricula.config.security.jwt;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.model.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Serviço responsável pelo gerenciamento de tokens JWT (JSON Web Tokens)
 * no sistema de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as operações de geração, validação e extração de informações
 * de tokens JWT, fornecendo autenticação stateless e segura para requisições
 * HTTP através de assinatura criptográfica.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Gera um token JWT para um usuário.
     *
     * <p>
     * Cria um token assinado contendo email do usuário como subject,
     * role como claim adicional e data de expiração configurada.
     * O token é assinado com a chave secreta configurada no sistema.
     * </p>
     *
     * @param user usuário para o qual o token será gerado
     * @return token JWT codificado e assinado
     */
    public String generateToken(User user) {

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrai o nome de usuário (email) de um token JWT.
     *
     * @param token token JWT codificado
     * @return email do usuário contido no token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai a data de expiração de um token JWT.
     *
     * @param token token JWT codificado
     * @return data e hora de expiração do token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai a role de autorização de um token JWT.
     *
     * @param token token JWT codificado
     * @return role do usuário (ADMIN ou STUDENT)
     */
    public UserRole extractRole(String token) {
        return UserRole.valueOf(
                extractClaim(token, claims -> claims.get("role", String.class)));
    }

    /**
     * Verifica se um token JWT expirou.
     *
     * @param token token JWT codificado
     * @return {@code true} se o token está expirado, {@code false} caso contrário
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida um token JWT verificando autenticidade e expiração.
     *
     * <p>
     * Verifica se o email contido no token corresponde ao usuário
     * e se o token ainda não expirou.
     * </p>
     *
     * @param token       token JWT codificado
     * @param userDetails detalhes do usuário para validação
     * @return {@code true} se o token é válido, {@code false} caso contrário
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}
