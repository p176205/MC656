package com.MC656.backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VotacaoTest {

    private final Assembleia assembleia = new Assembleia("Assembleia Geral", 10);

    private Votacao maioriaSimples(LocalDateTime inicio, LocalDateTime fim) {
        return new Votacao("Reforma", "Aprovar reforma da portaria", assembleia,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, inicio, fim);
    }

    @Test
    void deveCriarVotacaoAbertaDoTipoDeliberacao() {
        Votacao votacao = maioriaSimples(null, null);

        assertEquals(StatusVotacao.ABERTA, votacao.getStatus());
        assertEquals(TipoVotacao.DELIBERACAO, votacao.getTipo());
        assertSame(assembleia, votacao.getContexto());
    }

    @Test
    void deveRejeitarTituloVazio() {
        assertThrows(IllegalArgumentException.class, () -> new Votacao(" ", "desc", assembleia,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null));
    }

    @Test
    void deveRejeitarVotacaoSemContexto() {
        assertThrows(IllegalArgumentException.class, () -> new Votacao("Pauta", "desc", null,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null));
    }

    @Test
    void percentualMinimoDeveSerObrigatorioParaRegraPercentual() {
        assertThrows(IllegalArgumentException.class, () -> new Votacao("Pauta", "desc", assembleia,
                RegraAprovacao.PERCENTUAL_MINIMO, null, 50.0, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Votacao("Pauta", "desc", assembleia,
                RegraAprovacao.PERCENTUAL_MINIMO, 120.0, 50.0, null, null));
    }

    @Test
    void percentualMinimoDeveSerIgnoradoNaMaioriaSimples() {
        Votacao votacao = new Votacao("Pauta", "desc", assembleia,
                RegraAprovacao.MAIORIA_SIMPLES, 70.0, 50.0, null, null);

        assertNull(votacao.getPercentualMinimo());
    }

    @Test
    void deveRejeitarQuorumForaDaFaixa() {
        assertThrows(IllegalArgumentException.class, () -> new Votacao("Pauta", "desc", assembleia,
                RegraAprovacao.MAIORIA_SIMPLES, null, -1.0, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Votacao("Pauta", "desc", assembleia,
                RegraAprovacao.MAIORIA_SIMPLES, null, 101.0, null, null));
    }

    @Test
    void deveRejeitarFimAntesDoInicio() {
        LocalDateTime agora = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> maioriaSimples(agora, agora.minusHours(1)));
    }

    @Test
    void aceitaVotosApenasDentroDaJanela() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 1, 1, 12, 0);
        Votacao votacao = maioriaSimples(inicio, fim);

        assertFalse(votacao.aceitaVotos(inicio.minusMinutes(1)));
        assertTrue(votacao.aceitaVotos(inicio));
        assertTrue(votacao.aceitaVotos(fim.minusMinutes(1)));
        assertFalse(votacao.aceitaVotos(fim));
        assertTrue(votacao.prazoExpirado(fim));
    }

    @Test
    void naoAceitaVotosDepoisDeEncerrada() {
        Votacao votacao = maioriaSimples(null, null);

        votacao.encerrar();

        assertEquals(StatusVotacao.ENCERRADA, votacao.getStatus());
        assertFalse(votacao.aceitaVotos(LocalDateTime.now()));
        assertThrows(IllegalStateException.class, votacao::encerrar);
    }

    @Test
    void votoDeveTerPesoPositivo() {
        Votacao votacao = maioriaSimples(null, null);

        assertThrows(IllegalArgumentException.class, () -> new Voto(votacao, OpcaoVoto.SIM, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Voto(votacao, null, 1.0));
    }

    @Test
    void participacaoUsaPesoPadraoEValidaPeso() {
        Usuario usuario = new Usuario("Ana", "ana@email.com", "senha123");

        assertEquals(1.0, new Participacao(usuario, assembleia, Papel.MEMBRO).getPeso());
        assertThrows(IllegalArgumentException.class,
                () -> new Participacao(usuario, assembleia, Papel.MEMBRO, 0.0));
    }
}
