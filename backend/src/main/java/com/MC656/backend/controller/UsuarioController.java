package com.MC656.backend.controller;

import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioLoginDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.dto.UsuarioResponseLoginDTO;
import com.MC656.backend.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

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
    public ResponseEntity<?> login(@Valid @RequestBody UsuarioLoginDTO dto, HttpServletResponse response) {
        try {
            UsuarioResponseLoginDTO usuario = usuarioService.login(dto);

            ResponseCookie cookie = ResponseCookie.from("usuarioId", usuario.id().toString())
                    .httpOnly(true)
                    .secure(false) // true em produção, com HTTPS
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(Duration.ofDays(7))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getId(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        try {
            // Recupera o cookie existente
            System.out.println("Cookie: " + usuarioId);
            // Verifica se existe uma sessão
            if (usuarioId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Long id = Long.valueOf(usuarioId);
            UsuarioResponseDTO response = usuarioService.getUser(id);

            // Retorna o usuario
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            ResponseCookie cookie = ResponseCookie.from("usuarioId", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(0)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}