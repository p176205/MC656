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

import com.MC656.backend.entity.Condominio;
import com.MC656.backend.repository.ContextoRepository;

@WebMvcTest(ContextoController.class)
class ContextoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContextoRepository contextoRepository;

    @Test
    void deveRetornarTipoDoContextoParaExibirCamposEspecificosDaVotacao() throws Exception {
        when(contextoRepository.findById(12L)).thenReturn(Optional.of(new Condominio("Condomínio", 10)));

        mockMvc.perform(get("/api/contextos/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Condomínio"))
                .andExpect(jsonPath("$.tipo").value("CONDOMINIO"));
    }

    @Test
    void deveRetornar404ParaContextoInexistente() throws Exception {
        when(contextoRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/contextos/999"))
                .andExpect(status().isNotFound());
    }
}
