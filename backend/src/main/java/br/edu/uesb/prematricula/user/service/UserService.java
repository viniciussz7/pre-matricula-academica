package br.edu.uesb.prematricula.user.service;

import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.user.exception.UserNotFoundException;
import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    
}
