package com.MC656.backend.service;

import com.MC656.backend.dto.ApuracaoResponseDTO;
import com.MC656.backend.dto.VotacaoCreateDTO;
import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.dto.VotoRequestDTO;
import com.MC656.backend.entity.*;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.RegistroVotanteRepository;
import com.MC656.backend.repository.VotacaoRepository;
import com.MC656.backend.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotacaoServiceTest {

    @Mock private VotacaoRepository votacaoRepository;
    @Mock private VotoRepository votoRepository;
    @Mock private RegistroVotanteRepository registroVotanteRepository;
    @Mock private ContextoRepository contextoRepository;
    @Mock private ContextoService contextoService;

    private VotacaoService votacaoService;

    private Assembleia assembleia;
    private Condominio condominio;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        QuorumService quorumService = new QuorumService(List.of(new QuorumPorCabecaStrategy(), new QuorumPorPesoStrategy()));
        votacaoService = new VotacaoService(votacaoRepository, votoRepository, registroVotanteRepository,
                contextoRepository, contextoService, quorumService);

        assembleia = new Assembleia("Assembleia Geral", 10);
        ReflectionTestUtils.setField(assembleia, "id", 1L);
        condominio = new Condominio("Edifício Sol", 3);
        ReflectionTestUtils.setField(condominio, "id", 2L);

        usuario = new Usuario("João", "joao@email.com", "senha123");
        usuario.setId(7L);
    }

    private Votacao votacao(Contexto contexto, RegraAprovacao regra, Double percentual, double quorum) {
        Votacao v = new Votacao("Pauta", "Descrição", contexto, regra, percentual, quorum, null, null);
        ReflectionTestUtils.setField(v, "id", 100L);
        return v;
    }

    // ---------- abertura ----------

    @Test
    void abrir_Sucesso() {
        when(contextoRepository.findById(1L)).thenReturn(Optional.of(assembleia));
        when(votacaoRepository.save(any(Votacao.class))).thenAnswer(inv -> {
            Votacao v = inv.getArgument(0);
            ReflectionTestUtils.setField(v, "id", 100L);
            return v;
        });

        VotacaoResponseDTO response = votacaoService.abrir(new VotacaoCreateDTO(
                "Reforma", "Aprovar reforma", 1L, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null));

        assertEquals(100L, response.id());
        assertEquals(1L, response.contextoId());
        assertEquals(StatusVotacao.ABERTA, response.status());
        assertEquals(TipoVotacao.DELIBERACAO, response.tipo());
    }

    @Test
    void abrir_ContextoInexistente() {
        when(contextoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> votacaoService.abrir(new VotacaoCreateDTO(
                "Reforma", "Aprovar reforma", 99L, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null)));
        verify(votacaoRepository, never()).save(any());
    }

    // ---------- voto ----------

    @Test
    void votar_SalvaRegistroEVotoAnonimoComPesoDaParticipacao() {
        Votacao v = votacao(condominio, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(contextoService.buscarParticipacaoVotante(7L, 2L))
                .thenReturn(Optional.of(new Participacao(usuario, condominio, Papel.CONDOMINO, 2.5)));
        when(registroVotanteRepository.existsByVotacaoIdAndUsuarioId(100L, 7L)).thenReturn(false);

        votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM));

        ArgumentCaptor<RegistroVotante> registro = ArgumentCaptor.forClass(RegistroVotante.class);
        verify(registroVotanteRepository).save(registro.capture());
        assertEquals(usuario, registro.getValue().getUsuario());

        ArgumentCaptor<Voto> voto = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepository).save(voto.capture());
        assertEquals(OpcaoVoto.SIM, voto.getValue().getOpcao());
        assertEquals(2.5, voto.getValue().getPeso());
    }

    @Test
    void votar_UsuarioForaDoContexto() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(contextoService.buscarParticipacaoVotante(7L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM)));
        verifyNoInteractions(votoRepository);
    }

    @Test
    void votar_Duplicado() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(contextoService.buscarParticipacaoVotante(7L, 1L))
                .thenReturn(Optional.of(new Participacao(usuario, assembleia, Papel.PARTICIPANTE_ASSEMBLEIA)));
        when(registroVotanteRepository.existsByVotacaoIdAndUsuarioId(100L, 7L)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.NAO)));
        verifyNoInteractions(votoRepository);
    }

    @Test
    void votar_DuplicadoConcorrenteBarradoPelaConstraint() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(contextoService.buscarParticipacaoVotante(7L, 1L))
                .thenReturn(Optional.of(new Participacao(usuario, assembleia, Papel.PARTICIPANTE_ASSEMBLEIA)));
        when(registroVotanteRepository.existsByVotacaoIdAndUsuarioId(100L, 7L)).thenReturn(false);
        when(registroVotanteRepository.save(any())).thenThrow(new DataIntegrityViolationException("unique"));

        assertThrows(IllegalStateException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.NAO)));
        verifyNoInteractions(votoRepository);
    }

    @Test
    void votar_VotacaoEncerrada() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        v.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));

        assertThrows(IllegalStateException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM)));
        verifyNoInteractions(registroVotanteRepository, votoRepository);
    }

    @Test
    void votar_AntesDoInicio() {
        Votacao v = new Votacao("Pauta", "Descrição", assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0,
                LocalDateTime.now().plusDays(1), null);
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));

        assertThrows(IllegalStateException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM)));
    }

    @Test
    void votar_VotacaoInexistente() {
        when(votacaoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM)));
    }

    // ---------- encerramento e apuração ----------

    @Test
    void encerrar_JaEncerrada() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        v.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));

        assertThrows(IllegalStateException.class, () -> votacaoService.encerrar(100L));
    }

    @Test
    void apurar_VotacaoAbertaNaoMostraParcial() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));

        assertThrows(IllegalStateException.class, () -> votacaoService.apurar(100L));
        verifyNoInteractions(votoRepository);
    }

    @Test
    void apurar_PrazoExpiradoEncerraAutomaticamente() {
        Votacao v = new Votacao("Pauta", "Descrição", assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 0.0,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(votoRepository.findByVotacaoId(100L)).thenReturn(List.of());
        when(contextoService.pesosDoEleitorado(1L)).thenReturn(Map.of());

        ApuracaoResponseDTO apuracao = votacaoService.apurar(100L);

        assertEquals(StatusVotacao.ENCERRADA, v.getStatus());
        verify(votacaoRepository).save(v);
        assertEquals(ResultadoVotacao.REJEITADA, apuracao.resultado());
    }

    @Test
    void apurar_MaioriaSimplesAprovadaComAbstencaoContandoParaQuorum() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 75.0);
        v.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(votoRepository.findByVotacaoId(100L)).thenReturn(List.of(
                new Voto(v, OpcaoVoto.SIM, 1.0),
                new Voto(v, OpcaoVoto.SIM, 1.0),
                new Voto(v, OpcaoVoto.ABSTENCAO, 1.0)));
        when(contextoService.pesosDoEleitorado(1L)).thenReturn(Map.of(1L, 1.0, 2L, 1.0, 3L, 1.0, 4L, 1.0));

        ApuracaoResponseDTO apuracao = votacaoService.apurar(100L);

        assertTrue(apuracao.quorumAtingido()); // 3 de 4 = 75%
        assertEquals(ResultadoVotacao.APROVADA, apuracao.resultado());
        assertEquals(2.0, apuracao.votosSim());
        assertEquals(1.0, apuracao.abstencoes());
        assertEquals(3, apuracao.totalVotantes());
        assertEquals(100.0, apuracao.percentualSimObtido());
    }

    @Test
    void apurar_EmpateNaMaioriaSimplesRejeita() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 0.0);
        v.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(votoRepository.findByVotacaoId(100L)).thenReturn(List.of(
                new Voto(v, OpcaoVoto.SIM, 1.0),
                new Voto(v, OpcaoVoto.NAO, 1.0)));
        when(contextoService.pesosDoEleitorado(1L)).thenReturn(Map.of(1L, 1.0, 2L, 1.0));

        assertEquals(ResultadoVotacao.REJEITADA, votacaoService.apurar(100L).resultado());
    }

    @Test
    void apurar_SemQuorum() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        v.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(votoRepository.findByVotacaoId(100L)).thenReturn(List.of(new Voto(v, OpcaoVoto.SIM, 1.0)));
        when(contextoService.pesosDoEleitorado(1L)).thenReturn(Map.of(1L, 1.0, 2L, 1.0, 3L, 1.0));

        ApuracaoResponseDTO apuracao = votacaoService.apurar(100L);

        assertFalse(apuracao.quorumAtingido());
        assertEquals(ResultadoVotacao.SEM_QUORUM, apuracao.resultado());
    }

    @Test
    void apurar_CondominioPonderaPeloPesoDaUnidade() {
        // Um voto NAO com peso 3 vence dois votos SIM com peso 1
        Votacao v = votacao(condominio, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        v.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(votoRepository.findByVotacaoId(100L)).thenReturn(List.of(
                new Voto(v, OpcaoVoto.SIM, 1.0),
                new Voto(v, OpcaoVoto.SIM, 1.0),
                new Voto(v, OpcaoVoto.NAO, 3.0)));
        when(contextoService.pesosDoEleitorado(2L)).thenReturn(Map.of(1L, 1.0, 2L, 1.0, 3L, 3.0));

        ApuracaoResponseDTO apuracao = votacaoService.apurar(100L);

        assertEquals(ResultadoVotacao.REJEITADA, apuracao.resultado());
        assertEquals(2.0, apuracao.votosSim());
        assertEquals(3.0, apuracao.votosNao());
    }

    @Test
    void apurar_PercentualMinimo() {
        // 2 SIM x 1 NAO = 66,7% dos votos válidos; abstenção não entra na base
        Votacao v = votacao(assembleia, RegraAprovacao.PERCENTUAL_MINIMO, 66.0, 0.0);
        v.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        List<Voto> votos = List.of(
                new Voto(v, OpcaoVoto.SIM, 1.0),
                new Voto(v, OpcaoVoto.SIM, 1.0),
                new Voto(v, OpcaoVoto.NAO, 1.0),
                new Voto(v, OpcaoVoto.ABSTENCAO, 1.0));
        when(votoRepository.findByVotacaoId(100L)).thenReturn(votos);
        when(contextoService.pesosDoEleitorado(1L)).thenReturn(Map.of(1L, 1.0, 2L, 1.0, 3L, 1.0, 4L, 1.0));

        assertEquals(ResultadoVotacao.APROVADA, votacaoService.apurar(100L).resultado());

        Votacao exigente = votacao(assembleia, RegraAprovacao.PERCENTUAL_MINIMO, 70.0, 0.0);
        exigente.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(exigente));

        assertEquals(ResultadoVotacao.REJEITADA, votacaoService.apurar(100L).resultado());
    }
}
