package com.MC656.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MC656.backend.dto.VotacaoCadastroDTO;
import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.service.VotacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/votacoes")
public class VotacaoController {

    private final VotacaoService votacaoService;

    public VotacaoController(VotacaoService votacaoService) {
        this.votacaoService = votacaoService;
    }

    @PostMapping
    public ResponseEntity<VotacaoResponseDTO> cadastrar(@Valid @RequestBody VotacaoCadastroDTO dto) {
        VotacaoResponseDTO response = votacaoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/contexto/{contextoId}")
    public ResponseEntity<List<VotacaoResponseDTO>> listarPorContexto(@PathVariable Long contextoId) {
        return ResponseEntity.ok(votacaoService.listarPorContexto(contextoId));
    }
}
