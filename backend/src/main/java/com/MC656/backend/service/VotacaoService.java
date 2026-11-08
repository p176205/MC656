package com.MC656.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.MC656.backend.dto.VotacaoCadastroDTO;
import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.entity.Votacao;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.VotacaoRepository;

@Service
public class VotacaoService {

    private final VotacaoRepository votacaoRepository;
    private final ContextoRepository contextoRepository;

    public VotacaoService(VotacaoRepository votacaoRepository, ContextoRepository contextoRepository) {
        this.votacaoRepository = votacaoRepository;
        this.contextoRepository = contextoRepository;
    }

    public VotacaoResponseDTO cadastrar(VotacaoCadastroDTO dto) {
        Contexto contexto = contextoRepository.findById(dto.contextoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contexto não encontrado."));
        Votacao votacao = new Votacao(
                contexto,
                dto.nome(),
                dto.descricao(),
                dto.regraAprovacao(),
                dto.percentualMinimo(),
                dto.quorumMinimo(),
                dto.dataInicio(),
                dto.dataFim(),
                dto.status(),
                dto.pauta(),
                dto.participantesAptos(),
                dto.pesoVoto(),
                dto.unidadeApartamento(),
                dto.categoriaSegmento(),
                dto.elegibilidade());

        return toResponseDTO(votacaoRepository.save(votacao));
    }

    public List<VotacaoResponseDTO> listarPorContexto(Long contextoId) {
        if (!contextoRepository.existsById(contextoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contexto não encontrado.");
        }

        return votacaoRepository.findByContextoId(contextoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private VotacaoResponseDTO toResponseDTO(Votacao votacao) {
        return new VotacaoResponseDTO(
                votacao.getId(),
                votacao.getContexto().getId(),
                votacao.getNome(),
                votacao.getDescricao(),
                votacao.getRegraAprovacao(),
                votacao.getPercentualMinimo(),
                votacao.getQuorumMinimo(),
                votacao.getDataInicio(),
                votacao.getDataFim(),
                votacao.getStatus(),
                votacao.getPauta(),
                votacao.getParticipantesAptos(),
                votacao.getPesoVoto(),
                votacao.getUnidadeApartamento(),
                votacao.getCategoriaSegmento(),
                votacao.getElegibilidade());
    }
}
