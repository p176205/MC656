package com.MC656.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.entity.Votacao;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.VotacaoRepository;

@RestController
@RequestMapping("/api/votacoes")
public class VotacaoController {

    private final VotacaoRepository votacaoRepository;
    private final ContextoRepository contextoRepository;

    public VotacaoController(VotacaoRepository votacaoRepository, ContextoRepository contextoRepository) {
        this.votacaoRepository = votacaoRepository;
        this.contextoRepository = contextoRepository;
    }

    @GetMapping("/contexto/{contextoId}")
    public ResponseEntity<List<VotacaoResponseDTO>> listarPorContexto(@PathVariable Long contextoId) {
        if (!contextoRepository.existsById(contextoId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<VotacaoResponseDTO> votacoes = votacaoRepository.findByContextoId(contextoId).stream()
                .map(votacao -> toResponseDTO(votacao, contextoId))
                .toList();
        return ResponseEntity.ok(votacoes);
    }

    private VotacaoResponseDTO toResponseDTO(Votacao votacao, Long contextoId) {
        return new VotacaoResponseDTO(
                votacao.getId(),
                contextoId,
                votacao.getNome(),
                votacao.getDescricao(),
                votacao.getRegraAprovacao(),
                votacao.getPercentualMinimo(),
                votacao.getQuorumMinimo(),
                votacao.getDataInicio(),
                votacao.getDataFim(),
                votacao.getStatus());
    }
}
