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


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/first-access/request")
    public ResponseEntity<Void> requestFirstAccess(@Valid @RequestBody FirstAccessRequestDTO dto) {

        authService.requestFirstAccess(dto);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/first-access/confirm")
    public ResponseEntity<Void> confirmFirstAccess(@Valid @RequestBody ConfirmFirstAccessRequestDTO dto) {

        authService.confirmFirstAccess(dto);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        
        return ResponseEntity.ok(authService.login(dto));
    }
    
}
