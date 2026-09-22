package com.MC656.backend.controller;

import com.MC656.backend.entity.CentroAcademico;
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.entity.Papel;
import com.MC656.backend.entity.Participacao;
import com.MC656.backend.entity.Usuario;
import com.MC656.backend.repository.ParticipacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest; 
import org.springframework.test.context.bean.override.mockito.MockitoBean; 

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* isso eh um extra, garante que o endpoint retorna 200 OK e o JSON esperado,
 usando um participacaorepository mockado.
 */
@WebMvcTest(ParticipacaoController.class)
class ParticipacaoControllerTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @MockitoBean
    private ParticipacaoRepository participacaoRepository;

    @Test
    void deveRetornar200ComListaDeContextosDoUsuario() throws Exception {
        Usuario usuario = new Usuario("Thiago", "thiago@example.com", "senha123");
        Contexto contexto = new CentroAcademico("CA de Computação", 1);
        Participacao participacao = new Participacao(usuario, contexto, Papel.MEMBRO);

        when(participacaoRepository.findByUsuarioId(eq(1L)))
                .thenReturn(List.of(participacao));

        mockMvc.perform(get("/api/usuarios/1/contextos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("CA de Computação"))
                .andExpect(jsonPath("$[0].papel").value("MEMBRO"));
    }
}