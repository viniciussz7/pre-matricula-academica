package br.edu.uesb.prematricula.user.service;

import org.springframework.stereotype.Service;

import br.edu.uesb.prematricula.user.exception.EmailAlreadyExistsException;
import br.edu.uesb.prematricula.user.exception.UserNotFoundException;
import br.edu.uesb.prematricula.user.model.dto.request.CreateUserRequestDTO;
import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pelo gerenciamento de usuários do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas ao cadastro,
 * consulta e persistência de usuários, garantindo a unicidade de emails
 * e a consistência dos dados de autenticação.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Cria um novo usuário no sistema.
     *
     * <p>
     * Realiza validação de email único antes da persistência,
     * garantindo que não existam duplicatas no banco de dados.
     * </p>
     *
     * @param dto dados necessários para criação do usuário,
     *            contendo nome completo, email e role
     * @return usuário criado
     * @throws EmailAlreadyExistsException
     *                                     quando o email já existir no sistema
     */
    public User createUser(CreateUserRequestDTO dto) {

        validateEmail(dto.email());

        User user = User.builder()
                .fullName(dto.fullName())
                .email(dto.email())
                .role(dto.role())
                .build();

        return userRepository.save(user);
    }

    /**
     * Busca um usuário pelo email.
     *
     * @param email endereço de email do usuário
     * @return usuário encontrado
     * @throws UserNotFoundException
     *                               quando o email não existir
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    /**
     * Verifica se um email já existe no sistema.
     *
     * @param email endereço de email a verificar
     * @return {@code true} se o email existe, {@code false} caso contrário
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Persiste ou atualiza um usuário no banco de dados.
     *
     * @param user entidade de usuário a ser salva
     * @return usuário salvo
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }
    }

}
