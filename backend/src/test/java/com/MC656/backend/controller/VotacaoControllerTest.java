package com.MC656.backend.controller;

import com.MC656.backend.dto.ApuracaoResponseDTO;
import com.MC656.backend.dto.VotacaoCreateDTO;
import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.dto.VotoRequestDTO;
import com.MC656.backend.entity.*;
import com.MC656.backend.service.VotacaoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VotacaoController.class)
class VotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VotacaoService votacaoService;

    private final JsonMapper jsonMapper = new JsonMapper();

    private VotacaoResponseDTO resposta(StatusVotacao status) {
        return new VotacaoResponseDTO(100L, "Reforma", "Aprovar reforma", 1L, "Assembleia Geral",
                TipoVotacao.DELIBERACAO, RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, status, null, null);
    }

    @Test
    void abrir_Retorna201() throws Exception {
        VotacaoCreateDTO dto = new VotacaoCreateDTO("Reforma", "Aprovar reforma", 1L,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null);
        Mockito.when(votacaoService.abrir(any(VotacaoCreateDTO.class))).thenReturn(resposta(StatusVotacao.ABERTA));

        mockMvc.perform(post("/api/votacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.tipo").value("DELIBERACAO"));
    }

    @Test
    void abrir_SemTitulo_Retorna400() throws Exception {
        VotacaoCreateDTO dto = new VotacaoCreateDTO("", "Aprovar reforma", 1L,
                RegraAprovacao.MAIORIA_SIMPLES, null, 50.0, null, null);

        mockMvc.perform(post("/api/votacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        Mockito.verifyNoInteractions(votacaoService);
    }

    @Test
    void abrir_RegraInvalidaNaEntidade_Retorna400() throws Exception {
        VotacaoCreateDTO dto = new VotacaoCreateDTO("Reforma", "Aprovar reforma", 1L,
                RegraAprovacao.PERCENTUAL_MINIMO, null, 50.0, null, null);
        Mockito.when(votacaoService.abrir(any(VotacaoCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("O percentual mínimo deve estar entre 0 (exclusivo) e 100."));

        mockMvc.perform(post("/api/votacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscar_Inexistente_Retorna404() throws Exception {
        Mockito.when(votacaoService.buscar(99L)).thenThrow(new NoSuchElementException("Votação não encontrada."));

        mockMvc.perform(get("/api/votacoes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void votar_Retorna201SemCorpo() throws Exception {
        mockMvc.perform(post("/api/votacoes/100/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new VotoRequestDTO(7L, OpcaoVoto.SIM))))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        Mockito.verify(votacaoService).votar(eq(100L), any(VotoRequestDTO.class));
    }

    @Test
    void votar_SemOpcao_Retorna400() throws Exception {
        mockMvc.perform(post("/api/votacoes/100/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\": 7}"))
                .andExpect(status().isBadRequest());
        Mockito.verifyNoInteractions(votacaoService);
    }

    @Test
    void votar_Duplicado_Retorna409() throws Exception {
        Mockito.doThrow(new IllegalStateException("O usuário já votou nesta votação."))
                .when(votacaoService).votar(eq(100L), any(VotoRequestDTO.class));

        mockMvc.perform(post("/api/votacoes/100/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new VotoRequestDTO(7L, OpcaoVoto.SIM))))
                .andExpect(status().isConflict());
    }

    @Test
    void encerrar_Retorna200() throws Exception {
        Mockito.when(votacaoService.encerrar(100L)).thenReturn(resposta(StatusVotacao.ENCERRADA));

        mockMvc.perform(patch("/api/votacoes/100/encerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"));
    }

    @Test
    void apurar_Retorna200() throws Exception {
        Mockito.when(votacaoService.apurar(100L)).thenReturn(new ApuracaoResponseDTO(100L, "Reforma",
                ResultadoVotacao.APROVADA, true, 2.0, 1.0, 0.0, 3,
                RegraAprovacao.MAIORIA_SIMPLES, null, 66.67));

        mockMvc.perform(get("/api/votacoes/100/apuracao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("APROVADA"))
                .andExpect(jsonPath("$.totalVotantes").value(3));
    }

    @Test
    void apurar_VotacaoAberta_Retorna409() throws Exception {
        Mockito.when(votacaoService.apurar(100L))
                .thenThrow(new IllegalStateException("O resultado só fica disponível após o encerramento da votação."));

        mockMvc.perform(get("/api/votacoes/100/apuracao"))
                .andExpect(status().isConflict());
    }
}
