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

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<AdminResponseDTO> create(
            @Valid @RequestBody CreateAdminRequestDTO dto) {

        AdminResponseDTO response = adminService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> findAll() {

        return ResponseEntity.ok(adminService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(adminService.findById(id));
    }

}