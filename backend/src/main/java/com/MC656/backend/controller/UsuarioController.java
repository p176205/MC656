package com.MC656.backend.controller;

import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Aqui é o endpoint (rota) que o front vai usar para mandar e pegar dados
@RestController // Diz que essa classe possui endpoints HTTP da minha API.
@RequestMapping ("/api/usuarios") // endereco base (esse é o endereco completo q o front deve usar para fazer a requisição: http://localhost:8080/api/usuarios)
@CrossOrigin (origins = "http://localhost:3000")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    } // Spring cria uma instancia de UsuarioService e a fornece para o controller automaticamente

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody UsuarioCreateDTO dto) {
        try {
            UsuarioResponseDTO response = usuarioService.cadastrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}