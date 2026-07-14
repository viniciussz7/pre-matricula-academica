package br.edu.uesb.prematricula.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.admin.model.dto.response.AdminResponseDTO;
import br.edu.uesb.prematricula.auth.dto.request.ConfirmFirstAccessRequestDTO;
import br.edu.uesb.prematricula.auth.dto.request.FirstAccessRequestDTO;
import br.edu.uesb.prematricula.auth.dto.request.LoginRequestDTO;
import br.edu.uesb.prematricula.auth.dto.response.AuthResponseDTO;
import br.edu.uesb.prematricula.auth.exception.InvalidFirstAccessException;
import br.edu.uesb.prematricula.config.security.jwt.JwtService;
import br.edu.uesb.prematricula.student.model.dto.response.StudentResponseDTO;
import br.edu.uesb.prematricula.student.model.entity.Student;
import br.edu.uesb.prematricula.student.repository.StudentRepository;
import br.edu.uesb.prematricula.student.service.StudentService;
import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.service.UserService;
import br.edu.uesb.prematricula.util.EmailService;
import br.edu.uesb.prematricula.verificationtoken.model.entity.VerificationToken;
import br.edu.uesb.prematricula.verificationtoken.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela autenticação e autorização no sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas ao primeiro acesso,
 * autenticação de usuários e geração de tokens JWT, garantindo
 * a segurança e integridade do processo de login.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final EmailService emailService;

    /**
     * Solicita o token de primeiro acesso para um estudante.
     *
     * <p>
     * Realiza validações de segurança verificando o número de matrícula,
     * correspondência de email e se o primeiro acesso já foi realizado.
     * Gera e envia um token de primeiro acesso por email.
     * </p>
     *
     * @param dto dados necessários para solicitar primeiro acesso,
     *            contendo número de matrícula e email
     * @throws InvalidFirstAccessException
     *                                     quando matrícula e email não
     *                                     correspondem, ou se o
     *                                     primeiro acesso já foi completado
     */
    public void requestFirstAccess(FirstAccessRequestDTO dto) {

        Student student = studentService.findByRegistrationNumber(dto.registrationNumber());

        User user = student.getUser();

        if (!user.getEmail().equalsIgnoreCase(dto.email())) {
            throw new InvalidFirstAccessException("Invalid registration number or email.");
        }

        if (!user.isFirstAccess()) {
            throw new InvalidFirstAccessException("First access has already been completed.");
        }

        VerificationToken verificationToken = verificationTokenService.createFirstAccessToken(user);

        emailService.sendFirstAccessToken(
                user.getEmail(),
                verificationToken.getToken());

    }

    /**
     * Confirma o primeiro acesso do usuário e define sua senha.
     *
     * <p>
     * Valida o token de primeiro acesso, verifica correspondência
     * de senhas, encoda a nova senha, marca o primeiro acesso como
     * concluído e invalidar o token utilizado.
     * </p>
     *
     * @param dto dados necessários para confirmar primeiro acesso,
     *            contendo token e nova senha
     * @throws InvalidFirstAccessException
     *                                     quando o token for inválido ou as senhas
     *                                     não corresponderem
     */
    public void confirmFirstAccess(ConfirmFirstAccessRequestDTO dto) {

        VerificationToken verificationToken = verificationTokenService.validateToken(dto.token());

        User user = verificationToken.getUser();

        if (!dto.password().equals(dto.confirmPassword())) {
            throw new InvalidFirstAccessException("Passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(dto.password()));

        user.setFirstAccess(false);

        userService.save(user);

        verificationTokenService.markAsUsed(verificationToken);
    }

    /**
     * Realiza a autenticação de um usuário e gera token JWT.
     *
     * <p>
     * Autentica o usuário utilizando email e senha, gera um token JWT
     * com informações de autenticação e retorna um DTO contendo o token,
     * role do usuário e dados específicos do tipo de usuário (admin ou estudante).
     * </p>
     *
     * @param dto dados de login contendo email e senha do usuário
     * @return resposta contendo token JWT, role e dados do usuário
     * 
     *                                 
     */
    public AuthResponseDTO login(LoginRequestDTO dto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.email(),
                        dto.password()));

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        Object userDTO;
        if (user.getRole().name().equals("ADMIN")) {
            userDTO = new AdminResponseDTO(
                    user.getId(),
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.isActive());
        } else {
            Student student = studentRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Dados do estudante não encontrados"));

            userDTO = new StudentResponseDTO(
                    student.getId(),
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    student.getRegistrationNumber(),
                    student.isActive());
        }
        return new AuthResponseDTO(token, user.getRole().toString(), userDTO);
    }

}