package br.edu.uesb.prematricula.admin.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uesb.prematricula.admin.exception.AdminNotFoundException;
import br.edu.uesb.prematricula.admin.model.dto.request.CreateAdminRequestDTO;
import br.edu.uesb.prematricula.admin.model.dto.response.AdminResponseDTO;
import br.edu.uesb.prematricula.admin.model.entity.Admin;
import br.edu.uesb.prematricula.admin.repository.AdminRepository;
import br.edu.uesb.prematricula.user.model.dto.request.CreateUserRequestDTO;
import br.edu.uesb.prematricula.user.model.entity.User;
import br.edu.uesb.prematricula.user.model.enums.UserRole;
import br.edu.uesb.prematricula.user.service.UserService;
import br.edu.uesb.prematricula.util.EmailService;
import br.edu.uesb.prematricula.verificationtoken.model.entity.VerificationToken;
import br.edu.uesb.prematricula.verificationtoken.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pelo gerenciamento de administradores do sistema
 * de Pré-Matrícula Acadêmica.
 *
 * <p>
 * Centraliza as regras de negócio relacionadas ao cadastro,
 * consulta de administradores, além da integração com o serviço
 * de usuários e envio de tokens de primeiro acesso.
 * </p>
 *
 * @author Equipe de Desenvolvimento
 */
@Service
@RequiredArgsConstructor
public class AdminService {

        private final AdminRepository adminRepository;
        private final UserService userService;
        private final EmailService emailService;
        private final VerificationTokenService verificationTokenService;

        /**
         * Cadastra um novo administrador no sistema.
         *
         * <p>
         * Realiza a integração entre os serviços de usuário, admin e token,
         * criando um usuário com role ADMIN, vinculando-o à entidade Admin,
         * gerando um token de primeiro acesso e enviando por email.
         * </p>
         *
         * @param dto dados necessários para criação do administrador,
         *            contendo nome completo e email
         * @return administrador criado com seus dados associados
         */
        @Transactional
        public AdminResponseDTO create(
                        CreateAdminRequestDTO dto) {

                CreateUserRequestDTO userDTO = new CreateUserRequestDTO(
                                dto.fullName(),
                                dto.email(),
                                UserRole.ADMIN);

                User user = userService.createUser(userDTO);

                Admin admin = Admin.builder()
                                .user(user)
                                .build();

                Admin savedAdmin = adminRepository.save(admin);

                VerificationToken verificationToken = verificationTokenService
                                .createFirstAccessToken(user);

                emailService.sendFirstAccessToken(
                                user.getEmail(),
                                verificationToken.getToken());

                return toResponse(savedAdmin);
        }

        /**
         * Lista todos os administradores cadastrados.
         *
         * @return lista contendo todos os administradores
         */
        public List<AdminResponseDTO> findAll() {

                return adminRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();

        }

        /**
         * Busca um administrador pelo identificador.
         *
         * @param id identificador do administrador
         * @return administrador encontrado
         * @throws AdminNotFoundException
         *                                quando o identificador não existir
         */
        public AdminResponseDTO findById(UUID id) {

                Admin admin = getAdmin(id);

                return toResponse(admin);
        }

        private Admin getAdmin(UUID id) {

                return adminRepository.findById(id)
                                .orElseThrow(() -> new AdminNotFoundException("Admin not found."));
        }

        /**
         * Converte uma entidade de administrador para seu objeto de resposta.
         *
         * <p>
         * Realiza a transformação de dados da entidade JPA para o DTO
         * de resposta, extraindo informações associadas do usuário vinculado.
         * </p>
         *
         * @param admin entidade de administrador a ser convertida
         * @return objeto de resposta com os dados do administrador
         */
        // update()?

        private AdminResponseDTO toResponse(Admin admin) {

                return new AdminResponseDTO(
                                admin.getId(),
                                admin.getUser().getId(),
                                admin.getUser().getFullName(),
                                admin.getUser().getEmail(),
                                admin.isActive());

        }

}
