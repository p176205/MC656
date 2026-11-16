package com.MC656.backend.service;

import com.MC656.backend.dto.*;
import com.MC656.backend.entity.*;
import com.MC656.backend.repository.ChapaRepository;
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
    @Mock private ChapaRepository chapaRepository;

    private VotacaoService votacaoService;

    private Assembleia assembleia;
    private Condominio condominio;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        QuorumService quorumService = new QuorumService(List.of(new QuorumPorCabecaStrategy(), new QuorumPorPesoStrategy()));
        List<ApuracaoStrategy> apuracaoStrategies = List.of(new ApuracaoDeliberacaoStrategy(), new ApuracaoEleicaoStrategy());
        votacaoService = new VotacaoService(votacaoRepository, votoRepository, registroVotanteRepository,
                contextoRepository, contextoService, quorumService, chapaRepository, apuracaoStrategies);

        assembleia = new Assembleia("Assembleia Geral", 10);
        ReflectionTestUtils.setField(assembleia, "id", 1L);
        condominio = new Condominio("Edifício Sol", 3);
        ReflectionTestUtils.setField(condominio, "id", 2L);

        usuario = new Usuario("João", "joao@email.com", "senha123");
        usuario.setId(7L);
    }

    private Votacao votacao(Contexto contexto, RegraAprovacao regra, Double percentual, double quorum) {
        Votacao v = new Votacao("Pauta", "Descrição", contexto, TipoVotacao.DELIBERACAO, regra,
                percentual, quorum, null, null,
                contexto instanceof Assembleia ? "Pauta" : null,
                contexto instanceof Assembleia ? 10 : null,
                contexto instanceof Condominio ? java.math.BigDecimal.ONE : null,
                contexto instanceof Condominio ? "Apto 1" : null,
                contexto instanceof CentroAcademico ? "Graduação" : null,
                contexto instanceof CentroAcademico ? "Matriculados" : null);
        ReflectionTestUtils.setField(v, "id", 100L);
        return v;
    }

    // ---------- abertura ----------

    @Test
    void abrir_Sucesso() {
        when(contextoRepository.findById(1L)).thenReturn(Optional.of(assembleia));
        when(contextoService.isAdministradorDoContexto(7L, 1L)).thenReturn(true);
        when(votacaoRepository.save(any(Votacao.class))).thenAnswer(inv -> {
            Votacao v = inv.getArgument(0);
            ReflectionTestUtils.setField(v, "id", 100L);
            return v;
        });

        VotacaoResponseDTO response = votacaoService.abrir(new VotacaoCreateDTO(
                "Reforma", "Aprovar reforma", 1L, TipoVotacao.DELIBERACAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null,
                "Obras", 10, null, null, null, null, 7L, null));

        assertEquals(100L, response.id());
        assertEquals(1L, response.contextoId());
        assertEquals(StatusVotacao.ABERTA, response.status());
        assertEquals(TipoVotacao.DELIBERACAO, response.tipo());
        assertEquals("Obras", response.pauta());
    }

    @Test
    void abrir_DeliberacaoPorUsuarioNaoAdmin_LancaExcecao() {
        when(contextoRepository.findById(1L)).thenReturn(Optional.of(assembleia));
        when(contextoService.isAdministradorDoContexto(7L, 1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> votacaoService.abrir(new VotacaoCreateDTO(
                "Reforma", "Aprovar reforma", 1L, TipoVotacao.DELIBERACAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null,
                null, null, null, null, null, null, 7L, null)));
        verify(votacaoRepository, never()).save(any());
    }

    @Test
    void abrir_ContextoInexistente() {
        when(contextoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> votacaoService.abrir(new VotacaoCreateDTO(
                "Reforma", "Aprovar reforma", 99L, TipoVotacao.DELIBERACAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null,
                "Obras", 10, null, null, null, null, 7L, null)));
        verify(votacaoRepository, never()).save(any());
    }

    @Test
    void listarPorContexto_RetornaVotacoesDoContexto() {
        when(contextoRepository.findById(1L)).thenReturn(Optional.of(assembleia));
        when(votacaoRepository.findByContextoId(1L))
                .thenReturn(List.of(votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0)));

        List<VotacaoResponseDTO> respostas = votacaoService.listarPorContexto(1L);

        assertEquals(1, respostas.size());
        assertEquals(1L, respostas.get(0).contextoId());
    }

    @Test
    void listarPorContexto_ContextoInexistente() {
        when(contextoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> votacaoService.listarPorContexto(999L));
        verifyNoInteractions(votacaoRepository);
    }

    // ---------- voto ----------

    @Test
    void votar_SalvaRegistroEVotoAnonimoComPesoDaParticipacao() {
        Votacao v = votacao(condominio, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));
        when(contextoService.buscarParticipacaoVotante(7L, 2L))
                .thenReturn(Optional.of(new Participacao(usuario, condominio, Papel.CONDOMINO, 2.5)));
        when(registroVotanteRepository.existsByVotacaoIdAndUsuarioId(100L, 7L)).thenReturn(false);

        votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM, null));

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
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM, null)));
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
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.NAO, null)));
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
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.NAO, null)));
        verifyNoInteractions(votoRepository);
    }

    @Test
    void votar_VotacaoEncerrada() {
        Votacao v = votacao(assembleia, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0);
        v.encerrar();
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));

        assertThrows(IllegalStateException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM, null)));
        verifyNoInteractions(registroVotanteRepository, votoRepository);
    }

    @Test
    void votar_AntesDoInicio() {
        Votacao v = new Votacao("Pauta", "Descrição", assembleia, TipoVotacao.DELIBERACAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, LocalDateTime.now().plusDays(1), null,
                "Pauta", 10, null, null, null, null);
        when(votacaoRepository.findById(100L)).thenReturn(Optional.of(v));

        assertThrows(IllegalStateException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM, null)));
    }

    @Test
    void votar_VotacaoInexistente() {
        when(votacaoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> votacaoService.votar(100L, new VotoRequestDTO(7L, OpcaoVoto.SIM, null)));
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
        Votacao v = new Votacao("Pauta", "Descrição", assembleia, TipoVotacao.DELIBERACAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 0.0,
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

    // ---------- eleição de chapas ----------

    @Test
    void abrirEleicao_AdminCriaChapasComMembros_Sucesso() {
        Usuario membro1 = new Usuario("Membro 1", "m1@email.com", "senha123");
        membro1.setId(11L);
        Usuario membro2 = new Usuario("Membro 2", "m2@email.com", "senha123");
        membro2.setId(12L);

        when(contextoRepository.findById(1L)).thenReturn(Optional.of(assembleia));
        when(contextoService.isAdministradorDoContexto(7L, 1L)).thenReturn(true);
        when(contextoService.buscarMembroDoContexto(11L, 1L)).thenReturn(membro1);
        when(contextoService.buscarMembroDoContexto(12L, 1L)).thenReturn(membro2);
        when(votacaoRepository.save(any(Votacao.class))).thenAnswer(inv -> {
            Votacao v = inv.getArgument(0);
            ReflectionTestUtils.setField(v, "id", 200L);
            return v;
        });
        when(chapaRepository.save(any(Chapa.class))).thenAnswer(inv -> {
            Chapa c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", c.getNome().equals("Chapa A") ? 10L : 20L);
            return c;
        });

        VotacaoCreateDTO dto = new VotacaoCreateDTO(
                "Eleição Diretoria", "Escolha a nova gestão", 1L, TipoVotacao.ELEICAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null,
                null, null, null, null, null, null, 7L,
                List.of(
                        new ChapaCreateDTO("Chapa A", null, List.of(11L)),
                        new ChapaCreateDTO("Chapa B", null, List.of(12L))
                )
        );

        VotacaoResponseDTO response = votacaoService.abrir(dto);

        assertEquals(TipoVotacao.ELEICAO, response.tipo());
        assertEquals(2, response.chapas().size());
        assertEquals("Chapa A", response.chapas().get(0).nome());
        assertEquals(11L, response.chapas().get(0).membros().get(0).id());
    }

    @Test
    void abrirEleicao_UsuarioNaoAdmin_LancaExcecao() {
        when(contextoRepository.findById(1L)).thenReturn(Optional.of(assembleia));
        when(contextoService.isAdministradorDoContexto(7L, 1L)).thenReturn(false);

        VotacaoCreateDTO dto = new VotacaoCreateDTO(
                "Eleição Diretoria", "Escolha a nova gestão", 1L, TipoVotacao.ELEICAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null,
                null, null, null, null, null, null, 7L,
                List.of(new ChapaCreateDTO("Chapa A", null, List.of(11L)))
        );

        assertThrows(IllegalArgumentException.class, () -> votacaoService.abrir(dto));
        verify(votacaoRepository, never()).save(any());
    }

    @Test
    void atualizarChapa_MembroDaChapaPodeAlterarNomeEProposta() {
        Votacao eleicao = new Votacao("Eleição", "Descrição", assembleia, TipoVotacao.ELEICAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null);
        ReflectionTestUtils.setField(eleicao, "id", 200L);

        Chapa chapa = new Chapa(eleicao, "Chapa 1", null, List.of(usuario));
        ReflectionTestUtils.setField(chapa, "id", 10L);

        when(votacaoRepository.findById(200L)).thenReturn(Optional.of(eleicao));
        when(chapaRepository.findByIdAndVotacaoId(10L, 200L)).thenReturn(Optional.of(chapa));
        when(chapaRepository.findByVotacaoId(200L)).thenReturn(List.of(chapa));
        when(chapaRepository.save(any(Chapa.class))).thenAnswer(inv -> inv.getArgument(0));

        ChapaResponseDTO atualizada = votacaoService.atualizarChapa(
                200L, 10L, new ChapaUpdateDTO(7L, "Chapa Inovação", "Proposta de melhoria das salas"));

        assertEquals("Chapa Inovação", atualizada.nome());
        assertEquals("Proposta de melhoria das salas", atualizada.proposta());
    }

    @Test
    void atualizarChapa_UsuarioForaDaChapa_Negado() {
        Votacao eleicao = new Votacao("Eleição", "Descrição", assembleia, TipoVotacao.ELEICAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null);
        ReflectionTestUtils.setField(eleicao, "id", 200L);

        Chapa chapa = new Chapa(eleicao, "Chapa 1", null, List.of(usuario));
        ReflectionTestUtils.setField(chapa, "id", 10L);

        when(votacaoRepository.findById(200L)).thenReturn(Optional.of(eleicao));
        when(chapaRepository.findByIdAndVotacaoId(10L, 200L)).thenReturn(Optional.of(chapa));

        assertThrows(IllegalArgumentException.class, () -> votacaoService.atualizarChapa(
                200L, 10L, new ChapaUpdateDTO(999L, "Tentativa", "Proposta invasora")));
        verify(chapaRepository, never()).save(any());
    }

    @Test
    void apurarEleicao_MaioriaSimplesEEmpate() {
        Votacao eleicao = new Votacao("Eleição", "Descrição", assembleia, TipoVotacao.ELEICAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null);
        ReflectionTestUtils.setField(eleicao, "id", 200L);
        eleicao.encerrar();

        Chapa chapaA = new Chapa(eleicao, "Chapa A", "Proposta A", List.of(usuario));
        ReflectionTestUtils.setField(chapaA, "id", 10L);
        Chapa chapaB = new Chapa(eleicao, "Chapa B", "Proposta B", List.of(usuario));
        ReflectionTestUtils.setField(chapaB, "id", 20L);

        when(votacaoRepository.findById(200L)).thenReturn(Optional.of(eleicao));
        when(chapaRepository.findByVotacaoId(200L)).thenReturn(List.of(chapaA, chapaB));
        when(contextoService.pesosDoEleitorado(1L)).thenReturn(Map.of(1L, 1.0, 2L, 1.0, 3L, 1.0, 4L, 1.0));

        // Cenário 1: Chapa A vence com 2 votos contra 1 da Chapa B e 1 abstenção
        when(votoRepository.findByVotacaoId(200L)).thenReturn(List.of(
                Voto.emChapa(eleicao, chapaA, 1.0),
                Voto.emChapa(eleicao, chapaA, 1.0),
                Voto.emChapa(eleicao, chapaB, 1.0),
                new Voto(eleicao, OpcaoVoto.ABSTENCAO, 1.0)
        ));

        ApuracaoResponseDTO apuracao = votacaoService.apurar(200L);
        assertEquals(ResultadoVotacao.ELEITA, apuracao.resultado());
        assertNotNull(apuracao.chapaVencedora());
        assertEquals("Chapa A", apuracao.chapaVencedora().nome());
        assertEquals(2.0, apuracao.chapaVencedora().votos());
        assertEquals(1.0, apuracao.abstencoes());

        // Cenário 2: Empate entre Chapa A e Chapa B
        when(votoRepository.findByVotacaoId(200L)).thenReturn(List.of(
                Voto.emChapa(eleicao, chapaA, 1.0),
                Voto.emChapa(eleicao, chapaB, 1.0)
        ));

        ApuracaoResponseDTO apuracaoEmpate = votacaoService.apurar(200L);
        assertEquals(ResultadoVotacao.EMPATE, apuracaoEmpate.resultado());
        assertNull(apuracaoEmpate.chapaVencedora());
    }
}
