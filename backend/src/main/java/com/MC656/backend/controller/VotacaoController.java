package com.MC656.backend.controller;

import com.MC656.backend.dto.ApuracaoResponseDTO;
import com.MC656.backend.dto.VotacaoCreateDTO;
import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.dto.VotoRequestDTO;
import com.MC656.backend.service.VotacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

// Endpoints para abertura de pautas, votacao e apuracao
@RestController
@RequestMapping("/api/votacoes")
@CrossOrigin(origins = "http://localhost:3000")
public class VotacaoController {

    private final VotacaoService votacaoService;

    public VotacaoController(VotacaoService votacaoService) {
        this.votacaoService = votacaoService;
    }

    @PostMapping
    public ResponseEntity<VotacaoResponseDTO> abrir(@Valid @RequestBody VotacaoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(votacaoService.abrir(dto));
    }

    @GetMapping("/{id}")
    public VotacaoResponseDTO buscar(@PathVariable Long id) {
        return votacaoService.buscar(id);
    }

    @GetMapping("/contexto/{contextoId}")
    public List<VotacaoResponseDTO> listarPorContexto(@PathVariable Long contextoId) {
        return votacaoService.listarPorContexto(contextoId);
    }

    // Nao devolve a opcao escolhida: a resposta so confirma que o voto foi computado
    @PostMapping("/{id}/votos")
    public ResponseEntity<Void> votar(@PathVariable Long id, @Valid @RequestBody VotoRequestDTO dto) {
        votacaoService.votar(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/encerrar")
    public VotacaoResponseDTO encerrar(@PathVariable Long id) {
        return votacaoService.encerrar(id);
    }

    @GetMapping("/{id}/apuracao")
    public ApuracaoResponseDTO apurar(@PathVariable Long id) {
        return votacaoService.apurar(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> dadosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> naoEncontrado(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> conflito(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
