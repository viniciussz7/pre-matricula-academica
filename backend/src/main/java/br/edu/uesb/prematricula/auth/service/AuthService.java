package br.edu.uesb.prematricula.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.auth.dto.request.ConfirmFirstAccessRequestDTO;
import br.edu.uesb.prematricula.auth.dto.request.FirstAccessRequestDTO;
import br.edu.uesb.prematricula.auth.dto.request.LoginRequestDTO;
import br.edu.uesb.prematricula.auth.dto.response.AuthResponseDTO;
import br.edu.uesb.prematricula.auth.exception.InvalidFirstAccessException;
import br.edu.uesb.prematricula.config.security.jwt.JwtService;
import br.edu.uesb.prematricula.student.model.entity.Student;
import br.edu.uesb.prematricula.student.service.StudentService;
import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.service.UserService;
import br.edu.uesb.prematricula.util.EmailService;
import br.edu.uesb.prematricula.verificationtoken.model.entity.VerificationToken;
import br.edu.uesb.prematricula.verificationtoken.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentService studentService;
    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final EmailService emailService;


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
            verificationToken.getToken()
        );

    }


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


    public AuthResponseDTO login(LoginRequestDTO dto) {
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                dto.email(),
                dto.password()
            )
        );

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(token, user.getRole().toString(), user.getFullName().toString());
    }

}