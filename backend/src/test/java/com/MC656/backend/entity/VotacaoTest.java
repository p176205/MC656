package com.MC656.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class VotacaoTest {

    private final Contexto contexto = new Condominio("Condomínio teste", 10);
    private final LocalDateTime dataInicio = LocalDateTime.of(2026, 11, 1, 10, 0);
    private final LocalDateTime dataFim = LocalDateTime.of(2026, 11, 2, 10, 0);

    @Test
    void deveCriarVotacaoComDadosValidos() {
        Votacao votacao = criarVotacao(contexto, new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim);

        assertEquals(contexto, votacao.getContexto());
        assertEquals("Eleição de representante", votacao.getNome());
        assertEquals("Escolha do representante do condomínio.", votacao.getDescricao());
        assertEquals(RegraAprovacao.MAIORIA_SIMPLES, votacao.getRegraAprovacao());
        assertEquals(new BigDecimal("50"), votacao.getPercentualMinimo());
        assertEquals(new BigDecimal("25"), votacao.getQuorumMinimo());
        assertEquals(dataInicio, votacao.getDataInicio());
        assertEquals(dataFim, votacao.getDataFim());
        assertEquals(StatusVotacao.AGENDADA, votacao.getStatus());
    }

    @Test
    void naoDevePermitirCamposObrigatoriosAusentes() {
        assertThrows(IllegalArgumentException.class,
                () -> criarVotacao(null, new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim));
        assertThrows(IllegalArgumentException.class,
                () -> new Votacao(contexto, " ", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                        new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, StatusVotacao.AGENDADA));
        assertThrows(IllegalArgumentException.class,
                () -> new Votacao(contexto, "Nome", " ", RegraAprovacao.MAIORIA_SIMPLES,
                        new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, StatusVotacao.AGENDADA));
        assertThrows(IllegalArgumentException.class,
                () -> new Votacao(contexto, "Nome", "Descrição", null,
                        new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, StatusVotacao.AGENDADA));
        assertThrows(IllegalArgumentException.class,
                () -> new Votacao(contexto, "Nome", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                        new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, null));
    }

    @Test
    void deveAceitarPercentuaisNosLimitesERejeitarValoresForaDaFaixa() {
        criarVotacao(contexto, BigDecimal.ZERO, new BigDecimal("100"), dataInicio, dataFim);

        assertThrows(IllegalArgumentException.class,
                () -> criarVotacao(contexto, new BigDecimal("-0.01"), BigDecimal.ZERO, dataInicio, dataFim));
        assertThrows(IllegalArgumentException.class,
                () -> criarVotacao(contexto, BigDecimal.ZERO, new BigDecimal("100.01"), dataInicio, dataFim));
        assertThrows(IllegalArgumentException.class,
                () -> criarVotacao(contexto, null, BigDecimal.ZERO, dataInicio, dataFim));
    }

    @Test
    void deveExigirDataFimPosteriorADataInicio() {
        assertThrows(IllegalArgumentException.class,
                () -> criarVotacao(contexto, BigDecimal.ZERO, BigDecimal.ZERO, dataInicio, dataInicio));
        assertThrows(IllegalArgumentException.class,
                () -> criarVotacao(contexto, BigDecimal.ZERO, BigDecimal.ZERO, dataFim, dataInicio));
    }

    private Votacao criarVotacao(Contexto contexto, BigDecimal percentualMinimo, BigDecimal quorumMinimo,
            LocalDateTime dataInicio, LocalDateTime dataFim) {
        return new Votacao(contexto, "Eleição de representante", "Escolha do representante do condomínio.",
                RegraAprovacao.MAIORIA_SIMPLES, percentualMinimo, quorumMinimo,
                dataInicio, dataFim, StatusVotacao.AGENDADA);
    }
}
