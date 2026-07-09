package br.edu.uesb.prematricula.user.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.user.exception.EmailAlreadyExistsException;
import br.edu.uesb.prematricula.user.exception.UserNotFoundException;
import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.model.enums.UserRole;
import br.edu.uesb.prematricula.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String fullName, String email, UserRole userRole) {

        validateEmail(email);

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .role(userRole)
                .build();

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }
    }

}
