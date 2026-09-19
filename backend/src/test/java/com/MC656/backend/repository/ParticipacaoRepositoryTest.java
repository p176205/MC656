package com.MC656.backend.repository;
 
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.entity.Papel;
import com.MC656.backend.entity.Participacao;
import com.MC656.backend.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
 
import java.util.List;
 
import static org.assertj.core.api.Assertions.assertThat;
 
/* Salva um Usuario, um Contexto e uma Participacao e valida que o link
(FKs) e o papel foram persistidos corretamente 
Usa o UsuarioRepository feito no issue 9 e o
ContextoRepository provisorio (criei só p fazer funcionar ate
ter o do issue 8) 
 */
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
        Usuario usuario = usuarioRepository.save(new Usuario("Thiago", "thiago@example.com"));
        Contexto contexto = contextoRepository.save(new Contexto("Centro Acadêmico", "CA de Computação"));
 
        Participacao participacao = new Participacao(usuario, contexto, Papel.MEMBRO);
        participacaoRepository.save(participacao);
 
        List<Participacao> resultado = participacaoRepository.findByUsuarioId(usuario.getId());
 
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getContexto().getId()).isEqualTo(contexto.getId());
        assertThat(resultado.get(0).getPapel()).isEqualTo(Papel.MEMBRO);
    }
}
 
