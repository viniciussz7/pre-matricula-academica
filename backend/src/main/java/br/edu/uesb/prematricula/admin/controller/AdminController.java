package br.edu.uesb.prematricula.admin.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.uesb.prematricula.admin.model.dto.request.CreateAdminRequestDTO;
import br.edu.uesb.prematricula.admin.model.dto.response.AdminResponseDTO;
import br.edu.uesb.prematricula.admin.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsável pelo gerenciamento de usuários administradores.
 *
 * <p>
 * Disponibiliza endpoints REST para cadastro de administradores,
 * com geração automática de tokens de primeiro acesso e envio de
 * notificações por email.
 * </p>
 *
 * <p>
 * Os endpoints são protegidos pelo Spring Security,
 * permitindo acesso apenas a usuários autenticados com privilégios
 * de administrador.
 * </p>
 */
@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Cadastra um novo usuário administrador.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * Realiza integração com o serviço de geração de tokens de
     * primeiro acesso e envio de notificação por email.
     * </p>
     *
     * @param dto dados do administrador a ser criado
     * @return administrador criado com status HTTP 201
     */
    @PostMapping
    public ResponseEntity<AdminResponseDTO> create(
            @Valid @RequestBody CreateAdminRequestDTO dto) {

        AdminResponseDTO response = adminService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retorna todos os usuários administradores cadastrados.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @return lista de administradores
     */
    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> findAll() {

        return ResponseEntity.ok(adminService.findAll());
    }

    /**
     * Busca um administrador pelo identificador.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @param id identificador do administrador
     * @return administrador encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(adminService.findById(id));
    }

}