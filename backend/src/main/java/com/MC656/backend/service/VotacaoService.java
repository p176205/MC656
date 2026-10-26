package com.MC656.backend.service;

import com.MC656.backend.dto.ApuracaoResponseDTO;
import com.MC656.backend.dto.VotacaoCreateDTO;
import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.dto.VotoRequestDTO;
import com.MC656.backend.entity.*;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.RegistroVotanteRepository;
import com.MC656.backend.repository.VotacaoRepository;
import com.MC656.backend.repository.VotoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/* Ciclo de vida de uma votacao do tipo deliberacao: abertura, voto, encerramento e apuracao.
   Convencao de erros (traduzida para HTTP no VotacaoController):
   - IllegalArgumentException -> dados inválidos (400)
   - NoSuchElementException   -> recurso nao encontrado (404)
   - IllegalStateException    -> operacao incompativel com o estado da votacao (409) */
@Service
public class VotacaoService {

    private final VotacaoRepository votacaoRepository;
    private final VotoRepository votoRepository;
    private final RegistroVotanteRepository registroVotanteRepository;
    private final ContextoRepository contextoRepository;
    private final ContextoService contextoService;
    private final QuorumService quorumService;

    public VotacaoService(VotacaoRepository votacaoRepository,
                          VotoRepository votoRepository,
                          RegistroVotanteRepository registroVotanteRepository,
                          ContextoRepository contextoRepository,
                          ContextoService contextoService,
                          QuorumService quorumService) {
        this.votacaoRepository = votacaoRepository;
        this.votoRepository = votoRepository;
        this.registroVotanteRepository = registroVotanteRepository;
        this.contextoRepository = contextoRepository;
        this.contextoService = contextoService;
        this.quorumService = quorumService;
    }

    @Transactional
    public VotacaoResponseDTO abrir(VotacaoCreateDTO dto) {
        Contexto contexto = contextoRepository.findById(dto.contextoId())
                .orElseThrow(() -> new NoSuchElementException("Contexto não encontrado."));

        Votacao votacao = new Votacao(dto.titulo(), dto.descricao(), contexto, dto.regraAprovacao(),
                dto.percentualMinimo(), dto.quorumMinimo(), dto.dataInicio(), dto.dataFim());

        return toDTO(votacaoRepository.save(votacao));
    }

    @Transactional(readOnly = true)
    public VotacaoResponseDTO buscar(Long votacaoId) {
        return toDTO(buscarVotacao(votacaoId));
    }

    @Transactional(readOnly = true)
    public List<VotacaoResponseDTO> listarPorContexto(Long contextoId) {
        return votacaoRepository.findByContextoId(contextoId).stream()
                .map(this::toDTO)
                .toList();
    }

    /* Registra o voto em duas partes, na mesma transacao:
       1) RegistroVotante: marca que o usuario votou (impede voto duplo)
       2) Voto: guarda so opcao + peso, sem identificar o usuario (sigilo) */
    @Transactional
    public void votar(Long votacaoId, VotoRequestDTO dto) {
        Votacao votacao = buscarVotacao(votacaoId);

        if (!votacao.aceitaVotos(LocalDateTime.now())) {
            throw new IllegalStateException("A votação não está aberta para votos.");
        }

        Participacao participacao = contextoService
                .buscarParticipacaoVotante(dto.usuarioId(), votacao.getContexto().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "O usuário não participa do contexto desta votação."));

        if (registroVotanteRepository.existsByVotacaoIdAndUsuarioId(votacaoId, dto.usuarioId())) {
            throw new IllegalStateException("O usuário já votou nesta votação.");
        }

        try {
            registroVotanteRepository.save(new RegistroVotante(votacao, participacao.getUsuario()));
        } catch (DataIntegrityViolationException e) {
            // Duas requisicoes simultaneas do mesmo usuario: a constraint unica barra a segunda
            throw new IllegalStateException("O usuário já votou nesta votação.");
        }
        votoRepository.save(new Voto(votacao, dto.opcao(), participacao.getPeso()));
    }

    @Transactional
    public VotacaoResponseDTO encerrar(Long votacaoId) {
        Votacao votacao = buscarVotacao(votacaoId);
        votacao.encerrar();
        return toDTO(votacaoRepository.save(votacao));
    }

    // Resultado so eh exibido apos o encerramento (sem parciais durante a votacao)
    @Transactional
    public ApuracaoResponseDTO apurar(Long votacaoId) {
        Votacao votacao = buscarVotacao(votacaoId);

        // Encerramento automatico: se o prazo passou, a votacao é encerrada aqui
        if (votacao.prazoExpirado(LocalDateTime.now())) {
            votacao.encerrar();
            votacaoRepository.save(votacao);
        }
        if (votacao.getStatus() != StatusVotacao.ENCERRADA) {
            throw new IllegalStateException("O resultado só fica disponível após o encerramento da votação.");
        }

        List<Voto> votos = votoRepository.findByVotacaoId(votacaoId);
        Map<Long, Double> eleitorado = contextoService.pesosDoEleitorado(votacao.getContexto().getId());

        double sim = somaPesos(votos, OpcaoVoto.SIM);
        double nao = somaPesos(votos, OpcaoVoto.NAO);
        double abstencoes = somaPesos(votos, OpcaoVoto.ABSTENCAO);
        double validos = sim + nao;
        Double percentualSim = validos > 0 ? 100.0 * sim / validos : null;

        boolean quorumAtingido = quorumService.quorumAtingido(
                votacao.getContexto().getTipo(), votacao.getQuorumMinimo(), votos, eleitorado);

        ResultadoVotacao resultado;
        if (!quorumAtingido) {
            resultado = ResultadoVotacao.SEM_QUORUM;
        } else if (aprovada(votacao, sim, nao, percentualSim)) {
            resultado = ResultadoVotacao.APROVADA;
        } else {
            resultado = ResultadoVotacao.REJEITADA;
        }

        return new ApuracaoResponseDTO(votacao.getId(), votacao.getTitulo(), resultado, quorumAtingido,
                sim, nao, abstencoes, votos.size(), votacao.getRegraAprovacao(),
                votacao.getPercentualMinimo(), percentualSim);
    }

    private boolean aprovada(Votacao votacao, double sim, double nao, Double percentualSim) {
        return switch (votacao.getRegraAprovacao()) {
            case MAIORIA_SIMPLES -> sim > nao;
            case PERCENTUAL_MINIMO -> percentualSim != null && percentualSim >= votacao.getPercentualMinimo();
        };
    }

    private double somaPesos(List<Voto> votos, OpcaoVoto opcao) {
        return votos.stream()
                .filter(v -> v.getOpcao() == opcao)
                .mapToDouble(Voto::getPeso)
                .sum();
    }

    private Votacao buscarVotacao(Long votacaoId) {
        return votacaoRepository.findById(votacaoId)
                .orElseThrow(() -> new NoSuchElementException("Votação não encontrada."));
    }

    private VotacaoResponseDTO toDTO(Votacao v) {
        return new VotacaoResponseDTO(v.getId(), v.getTitulo(), v.getDescricao(),
                v.getContexto().getId(), v.getContexto().getNome(), v.getTipo(),
                v.getRegraAprovacao(), v.getPercentualMinimo(), v.getQuorumMinimo(),
                v.getStatus(), v.getDataInicio(), v.getDataFim());
    }
}
