package com.MC656.backend.service;

import com.MC656.backend.entity.*;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.ParticipacaoRepository;
import com.MC656.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextoServiceTest {

    @Mock
    private ContextoRepository contextoRepository;

    @Mock
    private ParticipacaoRepository participacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ContextoService contextoService;

    private final Condominio condominio = new Condominio("Edifício", 3);

    private Usuario usuario(long id) {
        Usuario u = new Usuario("U" + id, "u" + id + "@email.com", "senha123");
        u.setId(id);
        return u;
    }

    @Test
    void usuarioSemParticipacaoNaoEhVotante() {
        when(participacaoRepository.findByUsuarioIdAndContextoId(1L, 10L)).thenReturn(List.of());

        assertTrue(contextoService.buscarParticipacaoVotante(1L, 10L).isEmpty());
    }

    @Test
    void usuarioComDuasParticipacoesUsaOMaiorPeso() {
        Usuario u = usuario(1L);
        when(participacaoRepository.findByUsuarioIdAndContextoId(1L, 10L)).thenReturn(List.of(
                new Participacao(u, condominio, Papel.SINDICO, 1.0),
                new Participacao(u, condominio, Papel.CONDOMINO, 2.5)));

        Optional<Participacao> participacao = contextoService.buscarParticipacaoVotante(1L, 10L);

        assertEquals(2.5, participacao.orElseThrow().getPeso());
    }

    @Test
    void eleitoradoTemUmaEntradaPorUsuario() {
        Usuario u1 = usuario(1L);
        Usuario u2 = usuario(2L);
        when(participacaoRepository.findByContextoId(10L)).thenReturn(List.of(
                new Participacao(u1, condominio, Papel.SINDICO, 1.0),
                new Participacao(u1, condominio, Papel.CONDOMINO, 2.5),
                new Participacao(u2, condominio, Papel.CONDOMINO, 1.5)));

        Map<Long, Double> eleitorado = contextoService.pesosDoEleitorado(10L);

        assertEquals(Map.of(1L, 2.5, 2L, 1.5), eleitorado);
    }
}
