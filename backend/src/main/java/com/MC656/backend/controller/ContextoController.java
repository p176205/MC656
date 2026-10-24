package com.MC656.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.MC656.backend.dto.AssembleiaRequestDTO;
import com.MC656.backend.dto.CentroAcademicoRequestDTO;
import com.MC656.backend.dto.CondominioRequestDTO;
import com.MC656.backend.service.ContextoService;

@RestController // Diz que essa classe possui endpoints HTTP da minha API.
@RequestMapping("/api/contextos") // endereco base (esse é o endereco completo q o front deve usar para fazer requisição: http://localhost:8080/api/contextos)
@CrossOrigin(origins = "http://localhost:3000")
public class ContextoController {
    private final ContextoService contextoService;

    public ContextoController(ContextoService contextoService) {
        this.contextoService = contextoService;
    }

    @PostMapping("/assembleias")
    public ResponseEntity<?> criarAssembleia(
            @RequestBody AssembleiaRequestDTO dto) {

        try {
            var resposta = contextoService.criarAssembleia(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @PostMapping("/condominios")
    public ResponseEntity<?> criarCondominio(
            @RequestBody CondominioRequestDTO dto) {

        try {
            var resposta = contextoService.criarCondominio(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @PostMapping("/centros-academicos")
    public ResponseEntity<?> criarCentroAcademico(
            @RequestBody CentroAcademicoRequestDTO dto) {

        try {
            var resposta = contextoService.criarCentroAcademico(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> listarContextosDoUsuario(
            @PathVariable Long id) {

        try {
            var resposta = contextoService.listarContextosDoUsuario(id);
            System.out.println("Contextos encontrados: " + resposta);
            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
