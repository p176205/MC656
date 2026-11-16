package com.MC656.backend.service;

import com.MC656.backend.dto.ApuracaoResponseDTO;
import com.MC656.backend.entity.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApuracaoDeliberacaoStrategyTest {

    private final ApuracaoDeliberacaoStrategy strategy = new ApuracaoDeliberacaoStrategy();
    private final Assembleia contexto = new Assembleia("Assembleia", 10);

    private Votacao votacao(RegraAprovacao regra, Double percentualMinimo) {
        return new Votacao("Pauta", "Descrição", contexto, TipoVotacao.DELIBERACAO,
                regra, percentualMinimo, 0.0, null, null);
    }

    @Test
    void suportaApenasDeliberacao() {
        assertTrue(strategy.suporta(TipoVotacao.DELIBERACAO));
    }

    @Test
    void aprovaPorMaioriaSimples() {
        Votacao votacao = votacao(RegraAprovacao.MAIORIA_SIMPLES, null);
        ApuracaoResponseDTO apuracao = strategy.apurar(votacao, List.of(
                new Voto(votacao, OpcaoVoto.SIM, 2.0),
                new Voto(votacao, OpcaoVoto.NAO, 1.0)), List.of(), true);

        assertEquals(ResultadoVotacao.APROVADA, apuracao.resultado());
        assertEquals(2.0, apuracao.votosSim());
        assertEquals(1.0, apuracao.votosNao());
    }

    @Test
    void empateNaMaioriaSimplesResultaEmRejeicao() {
        Votacao votacao = votacao(RegraAprovacao.MAIORIA_SIMPLES, null);
        ApuracaoResponseDTO apuracao = strategy.apurar(votacao, List.of(
                new Voto(votacao, OpcaoVoto.SIM, 1.0),
                new Voto(votacao, OpcaoVoto.NAO, 1.0)), List.of(), true);

        assertEquals(ResultadoVotacao.REJEITADA, apuracao.resultado());
    }

    @Test
    void rejeitaQuandoNaoTemMaioriaSimples() {
        Votacao votacao = votacao(RegraAprovacao.MAIORIA_SIMPLES, null);
        ApuracaoResponseDTO apuracao = strategy.apurar(votacao, List.of(
                new Voto(votacao, OpcaoVoto.SIM, 1.0),
                new Voto(votacao, OpcaoVoto.NAO, 2.0)), List.of(), true);

        assertEquals(ResultadoVotacao.REJEITADA, apuracao.resultado());
    }

    @Test
    void faltaDeQuorumPrevaleceSobreVotosFavoraveis() {
        Votacao votacao = votacao(RegraAprovacao.MAIORIA_SIMPLES, null);
        ApuracaoResponseDTO apuracao = strategy.apurar(votacao,
                List.of(new Voto(votacao, OpcaoVoto.SIM, 5.0)), List.of(), false);

        assertEquals(ResultadoVotacao.SEM_QUORUM, apuracao.resultado());
        assertEquals(false, apuracao.quorumAtingido());
    }

    @Test
    void aplicaPercentualMinimoSobreVotosValidosEIgnoraAbstencao() {
        Votacao votacao = votacao(RegraAprovacao.PERCENTUAL_MINIMO, 66.0);
        ApuracaoResponseDTO apuracao = strategy.apurar(votacao, List.of(
                new Voto(votacao, OpcaoVoto.SIM, 2.0),
                new Voto(votacao, OpcaoVoto.NAO, 1.0),
                new Voto(votacao, OpcaoVoto.ABSTENCAO, 10.0)), List.of(), true);

        assertEquals(ResultadoVotacao.APROVADA, apuracao.resultado());
        assertEquals(10.0, apuracao.abstencoes());
        assertEquals(100.0 * 2.0 / 3.0, apuracao.percentualSimObtido());
        assertNull(apuracao.chapaVencedora());
    }
}
