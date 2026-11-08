package com.MC656.backend.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.MC656.backend.entity.CentroAcademico;
import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;
import com.MC656.backend.entity.Votacao;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.VotacaoRepository;

@WebMvcTest(VotacaoController.class)
class VotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VotacaoRepository votacaoRepository;

    @MockitoBean
    private ContextoRepository contextoRepository;

    @Test
    void deveRetornarListaDeVotacoesDoContextoComCamposCamelCase() throws Exception {
        when(contextoRepository.existsById(42L)).thenReturn(true);
        when(votacaoRepository.findByContextoId(eq(42L))).thenReturn(List.of(criarVotacao()));

        mockMvc.perform(get("/api/votacoes/contexto/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contextoId").value(42))
                .andExpect(jsonPath("$[0].nome").value("Eleição de representante"))
                .andExpect(jsonPath("$[0].descricao").value("Escolha do representante."))
                .andExpect(jsonPath("$[0].regraAprovacao").value("MAIORIA_SIMPLES"))
                .andExpect(jsonPath("$[0].percentualMinimo").value(60))
                .andExpect(jsonPath("$[0].quorumMinimo").value(25))
                .andExpect(jsonPath("$[0].dataInicio").value("2026-11-01T10:00:00"))
                .andExpect(jsonPath("$[0].dataFim").value("2026-11-02T10:00:00"))
                .andExpect(jsonPath("$[0].status").value("AGENDADA"));
    }

    @Test
    void deveRetornarListaVaziaQuandoContextoNaoTemVotacoes() throws Exception {
        when(contextoRepository.existsById(42L)).thenReturn(true);
        when(votacaoRepository.findByContextoId(eq(42L))).thenReturn(List.of());

        mockMvc.perform(get("/api/votacoes/contexto/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deveRetornar404QuandoContextoNaoExiste() throws Exception {
        when(contextoRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(get("/api/votacoes/contexto/99"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(votacaoRepository);
    }

    private Votacao criarVotacao() {
        return new Votacao(new CentroAcademico("Centro Acadêmico", 1),
                "Eleição de representante", "Escolha do representante.",
                RegraAprovacao.MAIORIA_SIMPLES, new BigDecimal("60"), new BigDecimal("25"),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 2, 10, 0),
                StatusVotacao.AGENDADA);
    }
}
