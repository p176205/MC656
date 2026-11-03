package com.MC656.backend.controller;

import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioLoginDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.dto.UsuarioResponseLoginDTO;
import com.MC656.backend.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Aqui é o endpoint (rota) que o front vai usar para mandar e pegar dados
@RestController // Diz que essa classe possui endpoints HTTP da minha API.
@RequestMapping("/api/usuarios") // endereco base (esse é o endereco completo q o front deve usar para fazer a
                                 // requisição: http://localhost:8080/api/usuarios)
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    } // Spring cria uma instancia de UsuarioService e a fornece para o controller
      // automaticamente

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody UsuarioCreateDTO dto) {
        try {
            UsuarioResponseDTO response = usuarioService.cadastrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UsuarioLoginDTO dto, HttpServletRequest request) {
        try {
            UsuarioResponseLoginDTO response = usuarioService.login(dto);
            HttpSession sessao = request.getSession(true);
            sessao.setAttribute("usuarioId", response.id());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getId(HttpServletRequest request) {
        try {
            // Recupera a sessão existente, sem criar uma nova
            HttpSession sessao = request.getSession(false);
            System.out.println("Sessão: " + sessao);
            // Verifica se existe uma sessão
            if (sessao == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Recupera o ID do usuário armazenado na sessão
            Long usuarioId = (Long) sessao.getAttribute("usuarioId");

            // Verifica se o ID está armazenado
            if (usuarioId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UsuarioResponseDTO response = usuarioService.getUser(usuarioId);

            // Retorna o usuario
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

}