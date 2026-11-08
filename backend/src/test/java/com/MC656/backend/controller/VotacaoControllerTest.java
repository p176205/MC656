package com.MC656.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.MC656.backend.dto.VotacaoCadastroDTO;
import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;
import com.MC656.backend.service.VotacaoService;

@WebMvcTest(VotacaoController.class)
class VotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VotacaoService votacaoService;

    @Test
    void deveRetornarListaDeVotacoesDoContextoComCamposCamelCase() throws Exception {
        when(votacaoService.listarPorContexto(42L)).thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/api/votacoes/contexto/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contextoId").value(42))
                .andExpect(jsonPath("$[0].nome").value("Eleição de representante"))
                .andExpect(jsonPath("$[0].descricao").value("Escolha do representante."))
                .andExpect(jsonPath("$[0].regraAprovacao").value("MAIORIA_SIMPLES"))
                .andExpect(jsonPath("$[0].percentualMinimo").value(60.5))
                .andExpect(jsonPath("$[0].quorumMinimo").value(25.25))
                .andExpect(jsonPath("$[0].dataInicio").value("2026-11-01T10:00:00"))
                .andExpect(jsonPath("$[0].dataFim").value("2026-11-02T10:00:00"))
                .andExpect(jsonPath("$[0].status").value("AGENDADA"))
                .andExpect(jsonPath("$[0].pesoVoto").value(1.5))
                .andExpect(jsonPath("$[0].unidadeApartamento").value("Apto 12"));
    }

    @Test
    void deveRetornarListaVaziaQuandoContextoNaoTemVotacoes() throws Exception {
        when(votacaoService.listarPorContexto(42L)).thenReturn(List.of());

        mockMvc.perform(get("/api/votacoes/contexto/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deveRetornar404QuandoContextoNaoExiste() throws Exception {
        when(votacaoService.listarPorContexto(99L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/votacoes/contexto/99"))
                .andExpect(status().isNotFound());

        verify(votacaoService).listarPorContexto(99L);
    }

    @Test
    void deveCadastrarVotacaoComNumerosEDatasISO8601() throws Exception {
        when(votacaoService.cadastrar(any(VotacaoCadastroDTO.class)))
                .thenReturn(criarResponse());

        mockMvc.perform(post("/api/votacoes")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "contextoId": 42,
                          "nome": "Eleição de representante",
                          "descricao": "Escolha do representante.",
                          "regraAprovacao": "MAIORIA_SIMPLES",
                          "percentualMinimo": 60.5,
                          "quorumMinimo": 25.25,
                          "dataInicio": "2026-11-01T10:00:00",
                          "dataFim": "2026-11-02T10:00:00",
                          "status": "AGENDADA",
                          "pesoVoto": 1.5,
                          "unidadeApartamento": "Apto 12"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.contextoId").value(42))
                .andExpect(jsonPath("$.nome").value("Eleição de representante"))
                .andExpect(jsonPath("$.regraAprovacao").value("MAIORIA_SIMPLES"))
                .andExpect(jsonPath("$.percentualMinimo").value(60.5))
                .andExpect(jsonPath("$.quorumMinimo").value(25.25))
                .andExpect(jsonPath("$.dataInicio").value("2026-11-01T10:00:00"))
                .andExpect(jsonPath("$.dataFim").value("2026-11-02T10:00:00"))
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.pesoVoto").value(1.5))
                .andExpect(jsonPath("$.unidadeApartamento").value("Apto 12"));

        verify(votacaoService).cadastrar(argThat(dto ->
                dto.contextoId().equals(42L)
                        && dto.percentualMinimo().compareTo(new BigDecimal("60.5")) == 0
                        && dto.quorumMinimo().compareTo(new BigDecimal("25.25")) == 0
                        && dto.pesoVoto().compareTo(new BigDecimal("1.5")) == 0
                        && dto.unidadeApartamento().equals("Apto 12")
                        && dto.dataInicio().equals(LocalDateTime.of(2026, 11, 1, 10, 0))
                        && dto.dataFim().equals(LocalDateTime.of(2026, 11, 2, 10, 0))));
    }

    @Test
    void deveRejeitarCadastroComCamposObrigatoriosOuPercentuaisInvalidos() throws Exception {
        mockMvc.perform(post("/api/votacoes")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "contextoId": 42,
                          "nome": " ",
                          "descricao": "",
                          "regraAprovacao": "MAIORIA_SIMPLES",
                          "percentualMinimo": 101,
                          "quorumMinimo": 25,
                          "dataInicio": "2026-11-01T10:00:00",
                          "dataFim": "2026-11-02T10:00:00",
                          "status": "AGENDADA"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(votacaoService);
    }

    @Test
    void deveRejeitarCadastroQuandoDataFimNaoForPosterior() throws Exception {
        mockMvc.perform(post("/api/votacoes")
                .contentType(APPLICATION_JSON)
                .content("""
                        {
                          "contextoId": 42,
                          "nome": "Eleição",
                          "descricao": "Descrição",
                          "regraAprovacao": "MAIORIA_SIMPLES",
                          "percentualMinimo": 60,
                          "quorumMinimo": 25,
                          "dataInicio": "2026-11-01T10:00:00",
                          "dataFim": "2026-11-01T10:00:00",
                          "status": "AGENDADA"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(votacaoService);
    }

    private VotacaoResponseDTO criarResponse() {
        return new VotacaoResponseDTO(
                12L, 42L, "Eleição de representante", "Escolha do representante.",
                RegraAprovacao.MAIORIA_SIMPLES, new BigDecimal("60.5"), new BigDecimal("25.25"),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 2, 10, 0),
                StatusVotacao.AGENDADA,
                null, null, new BigDecimal("1.5"), "Apto 12", null, null);
    }
}
