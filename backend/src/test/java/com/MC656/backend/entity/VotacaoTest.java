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
                        new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, StatusVotacao.AGENDADA,
                        null, null, BigDecimal.ONE, "Apto 1", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Votacao(contexto, "Nome", " ", RegraAprovacao.MAIORIA_SIMPLES,
                        new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, StatusVotacao.AGENDADA,
                        null, null, BigDecimal.ONE, "Apto 1", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Votacao(contexto, "Nome", "Descrição", null,
                        new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, StatusVotacao.AGENDADA,
                        null, null, BigDecimal.ONE, "Apto 1", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Votacao(contexto, "Nome", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                        new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, null,
                        null, null, BigDecimal.ONE, "Apto 1", null, null));
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

    @Test
    void deveExigirCamposEspecificosDoTipoDeContexto() {
        Assembleia assembleia = new Assembleia("Assembleia", 10);
        CentroAcademico centro = new CentroAcademico("Centro", 1);

        assertThrows(IllegalArgumentException.class, () -> new Votacao(
                assembleia, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                BigDecimal.ZERO, BigDecimal.ZERO, dataInicio, dataFim, StatusVotacao.AGENDADA,
                null, 10, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Votacao(
                assembleia, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                BigDecimal.ZERO, BigDecimal.ZERO, dataInicio, dataFim, StatusVotacao.AGENDADA,
                "Pauta", 0, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Votacao(
                contexto, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                BigDecimal.ZERO, BigDecimal.ZERO, dataInicio, dataFim, StatusVotacao.AGENDADA,
                null, null, BigDecimal.ZERO, "Apto 1", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Votacao(
                contexto, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                BigDecimal.ZERO, BigDecimal.ZERO, dataInicio, dataFim, StatusVotacao.AGENDADA,
                null, null, BigDecimal.ONE, " ", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Votacao(
                centro, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                BigDecimal.ZERO, BigDecimal.ZERO, dataInicio, dataFim, StatusVotacao.AGENDADA,
                null, null, null, null, "", "Elegível"));
        assertThrows(IllegalArgumentException.class, () -> new Votacao(
                centro, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                BigDecimal.ZERO, BigDecimal.ZERO, dataInicio, dataFim, StatusVotacao.AGENDADA,
                null, null, null, null, "Estudante", " "));
    }

    @Test
    void naoDeveAceitarCamposEspecificosDeOutroTipoDeContexto() {
        assertThrows(IllegalArgumentException.class, () -> new Votacao(
                contexto, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                BigDecimal.ZERO, BigDecimal.ZERO, dataInicio, dataFim, StatusVotacao.AGENDADA,
                "Pauta", null, BigDecimal.ONE, "Apto 1", null, null));
    }

    private Votacao criarVotacao(Contexto contexto, BigDecimal percentualMinimo, BigDecimal quorumMinimo,
            LocalDateTime dataInicio, LocalDateTime dataFim) {
        return new Votacao(contexto, "Eleição de representante", "Escolha do representante do condomínio.",
                RegraAprovacao.MAIORIA_SIMPLES, percentualMinimo, quorumMinimo,
                dataInicio, dataFim, StatusVotacao.AGENDADA,
                null, null, BigDecimal.ONE, "Apto 1", null, null);
    }
}
