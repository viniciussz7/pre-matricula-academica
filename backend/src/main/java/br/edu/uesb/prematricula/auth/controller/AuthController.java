package br.edu.uesb.prematricula.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uesb.prematricula.auth.dto.request.ConfirmFirstAccessRequestDTO;
import br.edu.uesb.prematricula.auth.dto.request.FirstAccessRequestDTO;
import br.edu.uesb.prematricula.auth.dto.request.LoginRequestDTO;
import br.edu.uesb.prematricula.auth.dto.response.AuthResponseDTO;
import br.edu.uesb.prematricula.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controlador responsável pelo gerenciamento de autenticação.
 *
 * <p>
 * Disponibiliza endpoints REST para o fluxo de autenticação completo,
 * incluindo solicitação de primeiro acesso, confirmação com definição
 * de senha e autenticação via JWT.
 * </p>
 *
 * <p>
 * Os endpoints não possuem proteção de autorização, permitindo
 * acesso público conforme definido na configuração de segurança.
 * </p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    /**
     * Solicita token de primeiro acesso para um usuário.
     *
     * <p>
     * Gera um token de 6 dígitos numéricos e o envia por email
     * para o usuário completar o processo de configuração inicial.
     * Disponível para acesso público.
     * </p>
     *
     * @param dto contendo o email do usuário
     * @return sem conteúdo (HTTP 200)
     */
    @PostMapping("/first-access/request")
    public ResponseEntity<Void> requestFirstAccess(@Valid @RequestBody FirstAccessRequestDTO dto) {

        authService.requestFirstAccess(dto);

        return ResponseEntity.ok().build();
    }

    /**
     * Confirma o primeiro acesso e define a senha do usuário.
     *
     * <p>
     * Valida o token recebido na etapa anterior e permite ao
     * usuário definir sua senha inicial. Disponível para acesso público.
     * </p>
     *
     * @param dto contendo email, token de confirmação e nova senha
     * @return sem conteúdo (HTTP 200)
     */
    @PostMapping("/first-access/confirm")
    public ResponseEntity<Void> confirmFirstAccess(@Valid @RequestBody ConfirmFirstAccessRequestDTO dto) {

        authService.confirmFirstAccess(dto);

        return ResponseEntity.ok().build();
    }

    /**
     * Autentica um usuário e retorna token JWT.
     *
     * <p>
     * Valida as credenciais (email e senha) e, em caso de sucesso,
     * retorna um token JWT para uso em subsequentes requisições.
     * Disponível para acesso público.
     * </p>
     *
     * @param dto contendo email e senha do usuário
     * @return resposta de autenticação com token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {

        return ResponseEntity.ok(authService.login(dto));
    }

}
