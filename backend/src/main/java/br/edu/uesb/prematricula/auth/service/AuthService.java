package br.edu.uesb.prematricula.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.config.security.jwt.JwtService;
import br.edu.uesb.prematricula.student.service.StudentService;
import br.edu.uesb.prematricula.user.repository.UserRepository;
import br.edu.uesb.prematricula.verificationtoken.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentService studentService;
    private final UserRepository userRepository;
    private final VerificationTokenService verificationTokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

}