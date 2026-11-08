package com.MC656.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.MC656.backend.entity.Condominio;
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;
import com.MC656.backend.entity.Votacao;

@DataJpaTest
class VotacaoRepositoryTest {

    @Autowired
    private VotacaoRepository votacaoRepository;

    @Autowired
    private ContextoRepository contextoRepository;

    @Test
    void deveConsultarVotacoesDoContextoEManterAssociacao() {
        Contexto contextoAlvo = contextoRepository.save(new Condominio("Condomínio A", 10));
        Contexto outroContexto = contextoRepository.save(new Condominio("Condomínio B", 20));
        Votacao votacaoDoContexto = votacaoRepository.save(criarVotacao(contextoAlvo, "Votação do condomínio A"));
        votacaoRepository.save(criarVotacao(outroContexto, "Votação do condomínio B"));

        List<Votacao> resultado = votacaoRepository.findByContextoId(contextoAlvo.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(votacaoDoContexto.getId());
        assertThat(resultado.get(0).getContexto().getId()).isEqualTo(contextoAlvo.getId());
        assertThat(resultado.get(0).getContexto().getNome()).isEqualTo("Condomínio A");
    }

    private Votacao criarVotacao(Contexto contexto, String nome) {
        return new Votacao(contexto, nome, "Descrição da votação", RegraAprovacao.MAIORIA_SIMPLES,
                new BigDecimal("50"), new BigDecimal("25"),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 2, 10, 0),
                StatusVotacao.AGENDADA, null, null, BigDecimal.ONE, "Apto 1", null, null);
    }
}
