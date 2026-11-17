package com.MC656.backend.service;

import com.MC656.backend.dto.ApuracaoResponseDTO;
import com.MC656.backend.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

/* Estrategia de apuracao para votacoes do tipo DELIBERACAO (pautas com SIM, NAO e ABSTENCAO).
   Abstencoes contam para o quorum, mas nao entram na base de calculo de aprovacao (SIM + NAO). */
@Component
public class ApuracaoDeliberacaoStrategy implements ApuracaoStrategy {

    @Override
    public boolean suporta(TipoVotacao tipoVotacao) {
        // Verifica se a estrategia eh valida de acordo com o tipo da votacao (deve ser ELEICAO)
        return tipoVotacao == TipoVotacao.DELIBERACAO;
    }

    @Override
    public ApuracaoResponseDTO apurar(Votacao votacao, List<Voto> votos, List<Chapa> chapas, boolean quorumAtingido) {
        // Metodo geral responsavel pela apuracao da DELIBERACAO
        double sim = somarPesosPorOpcao(votos, OpcaoVoto.SIM);
        double nao = somarPesosPorOpcao(votos, OpcaoVoto.NAO);
        double abstencoes = somarPesosPorOpcao(votos, OpcaoVoto.ABSTENCAO);

        double validos = sim + nao;
        Double percentualSim = validos > 0 ? 100.0 * sim / validos : null;

        ResultadoVotacao resultado = determinarResultado(votacao, quorumAtingido, sim, nao, percentualSim);

        return new ApuracaoResponseDTO(
                votacao.getId(),
                votacao.getTitulo(),
                resultado,
                quorumAtingido,
                sim,
                nao,
                abstencoes,
                votos.size(),
                votacao.getRegraAprovacao(),
                votacao.getPercentualMinimo(),
                percentualSim,
                List.of(),
                null
        );
    }

    private ResultadoVotacao determinarResultado(Votacao votacao, boolean quorumAtingido,
                                                 double sim, double nao, Double percentualSim) {
        // Define se a pauta foi aprovada, rejeitada ou invalidada por falta de quorum
        if (!quorumAtingido) {
            return ResultadoVotacao.SEM_QUORUM;
        }
        if (atingiuCriterioDeAprovacao(votacao, sim, nao, percentualSim)) {
            return ResultadoVotacao.APROVADA;
        }
        return ResultadoVotacao.REJEITADA;
    }

    private boolean atingiuCriterioDeAprovacao(Votacao votacao, double sim, double nao, Double percentualSim) {
        // Verifica a regra de aprovacao configurada na pauta (MAIORIA_SIMPLES ou PERCENTUAL_MINIMO)
        return switch (votacao.getRegraAprovacao()) {
            case MAIORIA_SIMPLES -> sim > nao;
            case PERCENTUAL_MINIMO -> percentualSim != null && percentualSim >= votacao.getPercentualMinimo();
        };
    }

    private double somarPesosPorOpcao(List<Voto> votos, OpcaoVoto opcao) {
        return votos.stream()
                .filter(v -> v.getOpcao() == opcao)
                .mapToDouble(Voto::getPeso)
                .sum();
    }
}

