package com.MC656.backend.repository;

import com.MC656.backend.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/* Valida o mapeamento JPA das novas tabelas (votacoes, votos, registros_votantes)
   e do peso em participacoes. */
@DataJpaTest
class VotacaoRepositoryTest {

    @Autowired private VotacaoRepository votacaoRepository;
    @Autowired private VotoRepository votoRepository;
    @Autowired private RegistroVotanteRepository registroVotanteRepository;
    @Autowired private ContextoRepository contextoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ParticipacaoRepository participacaoRepository;

    @Test
    void devePersistirVotacaoVotosEPesoDaParticipacao() {
        Contexto condominio = contextoRepository.save(new Condominio("Edifício Sol", 10));
        Usuario usuario = usuarioRepository.save(new Usuario("Ana", "ana@email.com", "senha123"));
        participacaoRepository.save(new Participacao(usuario, condominio, Papel.CONDOMINO, 2.5));

        Votacao votacao = votacaoRepository.save(new Votacao("Reforma", "Aprovar reforma", condominio,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null));
        votoRepository.save(new Voto(votacao, OpcaoVoto.SIM, 2.5));
        registroVotanteRepository.save(new RegistroVotante(votacao, usuario));

        assertThat(votacaoRepository.findByContextoId(condominio.getId())).hasSize(1);
        List<Voto> votos = votoRepository.findByVotacaoId(votacao.getId());
        assertThat(votos).hasSize(1);
        assertThat(votos.get(0).getPeso()).isEqualTo(2.5);
        assertThat(registroVotanteRepository.existsByVotacaoIdAndUsuarioId(votacao.getId(), usuario.getId())).isTrue();
        assertThat(participacaoRepository.findByUsuarioIdAndContextoId(usuario.getId(), condominio.getId())
                .get(0).getPeso()).isEqualTo(2.5);
    }

    @Test
    void constraintUnicaImpedeDoisRegistrosDoMesmoUsuarioNaMesmaVotacao() {
        Contexto assembleia = contextoRepository.save(new Assembleia("Assembleia", 5));
        Usuario usuario = usuarioRepository.save(new Usuario("Bia", "bia@email.com", "senha123"));
        Votacao votacao = votacaoRepository.save(new Votacao("Pauta", "Descrição", assembleia,
                RegraAprovacao.MAIORIA_SIMPLES, null, 0.0, null, null));

        registroVotanteRepository.saveAndFlush(new RegistroVotante(votacao, usuario));

        assertThatThrownBy(() -> registroVotanteRepository.saveAndFlush(new RegistroVotante(votacao, usuario)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
