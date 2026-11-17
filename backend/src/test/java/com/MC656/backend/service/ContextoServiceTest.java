package com.MC656.backend.service;

import com.MC656.backend.dto.CondominioRequestDTO;
import com.MC656.backend.dto.ContextoDetalheDTO;
import com.MC656.backend.dto.ContextoResponseDTO;
import com.MC656.backend.entity.*;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.ParticipacaoRepository;
import com.MC656.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    //  buscarParticipacaoVotante 

    @Test
    void usuarioSemParticipacaoNaoEhVotante() {
        when(participacaoRepository.findByUsuarioIdAndContextoId(1L, 10L)).thenReturn(List.of());

        assertTrue(contextoService.buscarParticipacaoVotante(1L, 10L).isEmpty());
    }

    @Test
    void usuarioComUmaUnicaParticipacaoRetornaEssaParticipacao() {
        Usuario u = usuario(1L);
        Participacao p = new Participacao(u, condominio, Papel.CONDOMINO, 1.0);
        when(participacaoRepository.findByUsuarioIdAndContextoId(1L, 10L)).thenReturn(List.of(p));

        assertSame(p, contextoService.buscarParticipacaoVotante(1L, 10L).orElseThrow());
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

    //  pesosDoEleitorado 

    @Test
    void eleitoradoVazioQuandoContextoNaoTemParticipacoes() {
        when(participacaoRepository.findByContextoId(10L)).thenReturn(List.of());

        assertTrue(contextoService.pesosDoEleitorado(10L).isEmpty());
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

    @Test
    void eleitoradoIndependeDaOrdemDasParticipacoes() {
        Usuario u1 = usuario(1L);
        when(participacaoRepository.findByContextoId(10L)).thenReturn(List.of(
                new Participacao(u1, condominio, Papel.CONDOMINO, 2.5),
                new Participacao(u1, condominio, Papel.SINDICO, 1.0)));

        assertEquals(Map.of(1L, 2.5), contextoService.pesosDoEleitorado(10L));
    }

    // buscarDetalhes (veio da develop) 

    @Test
    void buscaDetalhesDoContextoComTipo() {
        ReflectionTestUtils.setField(condominio, "id", 12L);
        when(contextoRepository.findById(12L)).thenReturn(Optional.of(condominio));

        ContextoDetalheDTO detalhes = contextoService.buscarDetalhes(12L);

        assertEquals(12L, detalhes.id());
        assertEquals("Edifício", detalhes.nome());
        assertEquals(TipoContexto.CONDOMINIO, detalhes.tipo());
    }

    @Test
    void contextoInexistenteNaoTemDetalhes() {
        when(contextoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> contextoService.buscarDetalhes(999L));
    }

    //  criarCondominio 

    @Test
    void criarCondominioRegistraCriadorComoAdministrador() {
        Usuario u = usuario(3L);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(u));
        when(contextoRepository.save(any(Condominio.class))).thenAnswer(inv -> inv.getArgument(0));

        Condominio criado = contextoService.criarCondominio(new CondominioRequestDTO(3L, "Edifício", 10));

        ArgumentCaptor<Participacao> captor = ArgumentCaptor.forClass(Participacao.class);
        verify(participacaoRepository).save(captor.capture());
        assertEquals(Papel.ADMINISTRADOR, captor.getValue().getPapel());
        assertEquals(u, captor.getValue().getUsuario());
        assertSame(criado, captor.getValue().getContexto());
    }

    @Test
    void criarCondominioLancaExcecaoSeUsuarioNaoExiste() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contextoService.criarCondominio(new CondominioRequestDTO(3L, "Edifício", 10)));

        verify(contextoRepository, never()).save(any());
        verify(participacaoRepository, never()).save(any());
    }

    //  listarContextosDoUsuario 

    @Test
    void listarContextosDoUsuarioMapeiaParticipacoesParaDTO() {
        Usuario u = usuario(1L);
        Participacao p = new Participacao(u, condominio, Papel.SINDICO, 1.0);
        when(participacaoRepository.findByUsuarioId(1L)).thenReturn(List.of(p));

        List<ContextoResponseDTO> resultado = contextoService.listarContextosDoUsuario(1L);

        assertEquals(1, resultado.size());
        assertEquals(condominio.getNome(), resultado.get(0).nome());
        assertEquals("SINDICO", resultado.get(0).papel());
    }

    @Test
    void listarContextosDoUsuarioRetornaListaVaziaSemParticipacoes() {
        when(participacaoRepository.findByUsuarioId(1L)).thenReturn(List.of());

        assertTrue(contextoService.listarContextosDoUsuario(1L).isEmpty());
    }

    //  isAdministradorDoContexto

    @Test
    void isAdministradorRetornaFalseSeIdsForemNulos() {
        assertFalse(contextoService.isAdministradorDoContexto(null, 10L));
        assertFalse(contextoService.isAdministradorDoContexto(1L, null));
    }

    @Test
    void isAdministradorRetornaTrueParaAdministradorOuSindico() {
        Usuario u = usuario(1L);
        when(participacaoRepository.findByUsuarioIdAndContextoId(1L, 10L)).thenReturn(List.of(
                new Participacao(u, condominio, Papel.SINDICO, 1.0)));

        assertTrue(contextoService.isAdministradorDoContexto(1L, 10L));
    }

    @Test
    void isAdministradorRetornaFalseParaCondominoComum() {
        Usuario u = usuario(1L);
        when(participacaoRepository.findByUsuarioIdAndContextoId(1L, 10L)).thenReturn(List.of(
                new Participacao(u, condominio, Papel.CONDOMINO, 1.0)));

        assertFalse(contextoService.isAdministradorDoContexto(1L, 10L));
    }

    //  buscarMembroDoContexto 

    @Test
    void buscarMembroRetornaUsuarioDaParticipacao() {
        Usuario u = usuario(1L);
        when(participacaoRepository.findByUsuarioIdAndContextoId(1L, 10L)).thenReturn(List.of(
                new Participacao(u, condominio, Papel.CONDOMINO, 1.0)));

        assertSame(u, contextoService.buscarMembroDoContexto(1L, 10L));
    }

    @Test
    void buscarMembroLancaExcecaoSeNaoParticipa() {
        when(participacaoRepository.findByUsuarioIdAndContextoId(1L, 10L)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> contextoService.buscarMembroDoContexto(1L, 10L));
    }

    //  entrarContexto 

    @Test
    void entrarContextoLancaExcecaoSeContextoNaoExiste() {
        UUID codigo = UUID.randomUUID();
        when(contextoRepository.findByCodigo(codigo)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> contextoService.entrarContexto(1L, codigo));

        verify(participacaoRepository, never()).save(any());
    }

    @Test
    void entrarContextoLancaExcecaoSeUsuarioJaParticipa() {
        UUID codigo = UUID.randomUUID();
        ReflectionTestUtils.setField(condominio, "id", 10L);
        when(contextoRepository.findByCodigo(codigo)).thenReturn(Optional.of(condominio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(participacaoRepository.existsByUsuario_IdAndContexto_Id(1L, 10L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> contextoService.entrarContexto(1L, codigo));

        verify(participacaoRepository, never()).save(any());
    }

    @Test
    void entrarContextoEmCondominioCriaParticipacaoComoCondomino() {
        UUID codigo = UUID.randomUUID();
        ReflectionTestUtils.setField(condominio, "id", 10L);
        when(contextoRepository.findByCodigo(codigo)).thenReturn(Optional.of(condominio));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(participacaoRepository.existsByUsuario_IdAndContexto_Id(1L, 10L)).thenReturn(false);

        ContextoResponseDTO resposta = contextoService.entrarContexto(1L, codigo);

        ArgumentCaptor<Participacao> captor = ArgumentCaptor.forClass(Participacao.class);
        verify(participacaoRepository).save(captor.capture());
        assertEquals(Papel.CONDOMINO, captor.getValue().getPapel());
        assertEquals("CONDOMINO", resposta.papel());
    }
}