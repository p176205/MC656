package com.MC656.backend.service;

import com.MC656.backend.entity.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QuorumServiceTest {

    private final QuorumService quorumService =
            new QuorumService(List.of(new QuorumPorCabecaStrategy(), new QuorumPorPesoStrategy()));

    private final Votacao votacao = new Votacao("Pauta", "desc", new Condominio("Edifício", 4),
            RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null);

    @Test
    void assembleiaContaPorCabeca() {
        // 4 aptos, 2 votaram = 50% -> atinge 50%, mesmo com pesos diferentes
        Map<Long, Double> eleitorado = Map.of(1L, 1.0, 2L, 1.0, 3L, 1.0, 4L, 1.0);
        List<Voto> votos = List.of(new Voto(votacao, OpcaoVoto.SIM, 1.0), new Voto(votacao, OpcaoVoto.ABSTENCAO, 1.0));

        assertTrue(quorumService.quorumAtingido(TipoContexto.ASSEMBLEIA, 50.0, votos, eleitorado));
        assertFalse(quorumService.quorumAtingido(TipoContexto.CENTRO_ACADEMICO, 51.0, votos, eleitorado));
    }

    @Test
    void condominioContaPorPeso() {
        // Peso total 10; um unico votante com peso 6 -> 60%
        Map<Long, Double> eleitorado = Map.of(1L, 6.0, 2L, 2.0, 3L, 2.0);
        List<Voto> votos = List.of(new Voto(votacao, OpcaoVoto.NAO, 6.0));

        assertTrue(quorumService.quorumAtingido(TipoContexto.CONDOMINIO, 60.0, votos, eleitorado));
        assertFalse(quorumService.quorumAtingido(TipoContexto.CONDOMINIO, 61.0, votos, eleitorado));
    }

    @Test
    void eleitoradoVazioNaoAtingeQuorumExcetoSeQuorumForZero() {
        assertFalse(quorumService.quorumAtingido(TipoContexto.ASSEMBLEIA, 10.0, List.of(), Map.of()));
        assertFalse(quorumService.quorumAtingido(TipoContexto.CONDOMINIO, 10.0, List.of(), Map.of()));
        assertTrue(quorumService.quorumAtingido(TipoContexto.ASSEMBLEIA, 0.0, List.of(), Map.of()));
        assertTrue(quorumService.quorumAtingido(TipoContexto.CONDOMINIO, 0.0, List.of(), Map.of()));
    }

    @Test
    void semEstrategiaParaOContextoLancaErro() {
        QuorumService semEstrategias = new QuorumService(List.of());

        assertThrows(IllegalStateException.class,
                () -> semEstrategias.quorumAtingido(TipoContexto.ASSEMBLEIA, 50.0, List.of(), Map.of()));
    }
}
