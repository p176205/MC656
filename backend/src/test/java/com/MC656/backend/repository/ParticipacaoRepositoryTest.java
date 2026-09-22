package com.MC656.backend.repository;

import com.MC656.backend.entity.CentroAcademico;
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.entity.Papel;
import com.MC656.backend.entity.Participacao;
import com.MC656.backend.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest; // Spring Boot 4: pacote novo

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/* salva um Usuario, um Contexto e uma Participacao, e valida que o linke o papel foram persistidos corretamente*/
@DataJpaTest
class ParticipacaoRepositoryTest {

    @Autowired
    private ParticipacaoRepository participacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContextoRepository contextoRepository;

    @Test
    void devePersistirParticipacaoComPapelEVincularUsuarioEContexto() {
        Usuario usuario = usuarioRepository.save(new Usuario("Thiago", "thiago@example.com", "senha123"));
        Contexto contexto = contextoRepository.save(new CentroAcademico("CA de Computação", 1));

        Participacao participacao = new Participacao(usuario, contexto, Papel.MEMBRO);
        participacaoRepository.save(participacao);

        List<Participacao> resultado = participacaoRepository.findByUsuarioId(usuario.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getContexto().getId()).isEqualTo(contexto.getId());
        assertThat(resultado.get(0).getPapel()).isEqualTo(Papel.MEMBRO);
    }
}