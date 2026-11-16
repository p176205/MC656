package com.MC656.backend.controller;

import com.MC656.backend.dto.UsuarioCreateDTO;
import com.MC656.backend.dto.UsuarioLoginDTO;
import com.MC656.backend.dto.UsuarioResponseDTO;
import com.MC656.backend.dto.UsuarioResponseLoginDTO;
import com.MC656.backend.service.UsuarioService;
import jakarta.servlet.http.Cookie;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private JsonMapper jsonMapper = new JsonMapper();

    //  cadastrar 

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

    // login 

    @Test
    public void testLogin_Sucesso_RetornaIdEDefineCookie() throws Exception {
        UsuarioLoginDTO requestDTO = new UsuarioLoginDTO("joao@email.com", "senha123");

        Mockito.when(usuarioService.login(any(UsuarioLoginDTO.class)))
               .thenReturn(new UsuarioResponseLoginDTO(1L));

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("usuarioId=1")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")));

        Mockito.verify(usuarioService).login(Mockito.argThat(dto -> dto.email().equals("joao@email.com")));
    }

    @Test
    public void testLogin_CredenciaisInvalidas_ReturnsBadRequestSemCookie() throws Exception {
        UsuarioLoginDTO requestDTO = new UsuarioLoginDTO("joao@email.com", "errada");

        Mockito.when(usuarioService.login(any(UsuarioLoginDTO.class)))
               .thenThrow(new IllegalArgumentException("Credenciais invalidas"));

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Credenciais invalidas"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    public void testLogin_EmailEmBranco_ReturnsBadRequest() throws Exception {
        UsuarioLoginDTO requestDTO = new UsuarioLoginDTO("", "senha123");

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(usuarioService);
    }

    @Test
    public void testLogin_EmailInvalido_ReturnsBadRequest() throws Exception {
        UsuarioLoginDTO requestDTO = new UsuarioLoginDTO("email-invalido", "senha123");

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(usuarioService);
    }

    @Test
    public void testLogin_SenhaEmBranco_ReturnsBadRequest() throws Exception {
        UsuarioLoginDTO requestDTO = new UsuarioLoginDTO("joao@email.com", "");

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(usuarioService);
    }

    @Test
    public void testLogin_BodyAusente_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // getId (/me)

    @Test
    public void testGetId_ComCookie_RetornaUsuario() throws Exception {
        Mockito.when(usuarioService.getUser(1L))
               .thenReturn(new UsuarioResponseDTO(1L, "João", "joao@email.com"));

        mockMvc.perform(get("/api/usuarios/me")
                .cookie(new Cookie("usuarioId", "1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    public void testGetId_SemCookie_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized());

        Mockito.verifyNoInteractions(usuarioService);
    }

    @Test
    public void testGetId_UsuarioNaoEncontrado_ReturnsBadRequest() throws Exception {
        Mockito.when(usuarioService.getUser(99L))
               .thenThrow(new IllegalArgumentException("Usuario nao encontrado"));

        mockMvc.perform(get("/api/usuarios/me")
                .cookie(new Cookie("usuarioId", "99")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuario nao encontrado"));
    }

    @Test
    public void testGetId_CookieNaoNumerico_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/usuarios/me")
                .cookie(new Cookie("usuarioId", "abc")))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(usuarioService);
    }

    // logout 

    @Test
    public void testLogout_RetornaOkEExpiraCookie() throws Exception {
        mockMvc.perform(post("/api/usuarios/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("usuarioId=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")));

        Mockito.verifyNoInteractions(usuarioService);
    }

    @Test
    public void testLogout_FuncionaMesmoSemCookiePrevio() throws Exception {
        mockMvc.perform(post("/api/usuarios/logout"))
                .andExpect(status().isOk());
    }
}