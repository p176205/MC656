package com.MC656.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.MC656.backend.dto.ContextoDetalheDTO;
import com.MC656.backend.entity.TipoContexto;
import com.MC656.backend.service.ContextoService;
import java.util.NoSuchElementException;

@WebMvcTest(ContextoController.class)
class ContextoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContextoService contextoService;

    @Test
    void deveRetornarTipoDoContextoParaExibirCamposEspecificosDaVotacao() throws Exception {
        when(contextoService.buscarDetalhes(12L))
                .thenReturn(new ContextoDetalheDTO(12L, "Condomínio", TipoContexto.CONDOMINIO));

        mockMvc.perform(get("/api/contextos/id/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Condomínio"))
                .andExpect(jsonPath("$.tipo").value("CONDOMINIO"));
    }

    @Test
    void deveRetornar404ParaContextoInexistente() throws Exception {
        when(contextoService.buscarDetalhes(999L))
                .thenThrow(new NoSuchElementException("Contexto não encontrado."));

        mockMvc.perform(get("/api/contextos/id/999"))
                .andExpect(status().isNotFound());
    }
}
