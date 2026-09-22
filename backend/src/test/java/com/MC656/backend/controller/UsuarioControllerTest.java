package com.MC656.backend.controller;

import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.service.UsuarioService;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private JsonMapper jsonMapper = new JsonMapper();

    @Test
    public void testCadastrarUsuario_Sucesso() throws Exception {
        UsuarioCreateDTO requestDTO = new UsuarioCreateDTO("João", "joao@email.com", "senha123");
        UsuarioResponseDTO responseDTO = new UsuarioResponseDTO(1L, "João", "joao@email.com");

        Mockito.when(usuarioService.cadastrar(any(UsuarioCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        Mockito.verify(usuarioService).cadastrar(Mockito.argThat(dto -> dto.email().equals("joao@email.com")));
    }

    @Test
    public void testCadastrarUsuario_EmailNaoFornecido_ReturnsBadRequest() throws Exception {
        UsuarioCreateDTO requestDTO = new UsuarioCreateDTO("João", "", "senha123");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCadastrarUsuario_NomeEmBranco_ReturnsBadRequest() throws Exception {
        UsuarioCreateDTO requestDTO = new UsuarioCreateDTO("", "joao@email.com", "senha123");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCadastrarUsuario_EmailInvalido_ReturnsBadRequest() throws Exception {
        UsuarioCreateDTO requestDTO = new UsuarioCreateDTO("João", "email-invalido", "senha123");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCadastrarUsuario_SenhaVazia_ReturnsBadRequest() throws Exception {
        UsuarioCreateDTO requestDTO = new UsuarioCreateDTO("João", "joao@email.com", "");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCadastrarUsuario_EmailDuplicado_ReturnsBadRequest() throws Exception {
        UsuarioCreateDTO requestDTO = new UsuarioCreateDTO("João", "joao@email.com", "senha123");

        Mockito.when(usuarioService.cadastrar(any(UsuarioCreateDTO.class)))
               .thenThrow(new IllegalArgumentException("E-mail já cadastrado"));

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCadastrarUsuario_BodyAusente_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}