package com.MC656.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.MC656.backend.dto.AssembleiaRequestDTO;
import com.MC656.backend.dto.CentroAcademicoRequestDTO;
import com.MC656.backend.dto.CondominioRequestDTO;
import com.MC656.backend.dto.ContextoDetalheDTO;
import com.MC656.backend.dto.ContextoResponseDTO;
import com.MC656.backend.dto.EntrarContextoRequestDTO;
import com.MC656.backend.entity.Assembleia;
import com.MC656.backend.entity.CentroAcademico;
import com.MC656.backend.entity.Condominio;
import com.MC656.backend.entity.TipoContexto;
import com.MC656.backend.service.ContextoService;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(ContextoController.class)
class ContextoControllerTest {

        @Autowired
        private MockMvc mockMvc; // Instancia de MockMvc para simular requisicoes HTTP

        @MockitoBean
        private ContextoService contextoService; // Como este controller recebe o Service pelo construtor e, nesse
                                                 // teste,
                                                 // nao queremos utilizar o service real, o @MockitoBean cria um objeto
                                                 // simulado do Service

        @Autowired
        private JsonMapper jsonMapper;

        // Testes de criação de assembleia

        @Test
        void deveCriarAssembleiaComSucesso() throws Exception {

                // Prepara os dados da requisição
                AssembleiaRequestDTO requestDTO = new AssembleiaRequestDTO(3L, "Assembleia1", 30);

                // Prepara a resposta que o Service deverá retornar
                Assembleia responseDTO = new Assembleia("Assembleia1", 30);

                // Simula o comportamento do Service
                when(contextoService.criarAssembleia(any(AssembleiaRequestDTO.class)))
                                .thenReturn(responseDTO);

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/assembleias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica o status HTTP 201
                                .andExpect(status().isCreated());

                // Verifica se o Service foi chamado
                verify(contextoService)
                                .criarAssembleia(any(AssembleiaRequestDTO.class));
        }

        @Test
        void deveRetornarBadRequestQuandoAssembleiaForInvalida() throws Exception {

                // Prepara uma requisição inválida
                AssembleiaRequestDTO requestDTO = new AssembleiaRequestDTO(3L, null, 30);

                // Simula o Service lançando uma exceção
                when(contextoService.criarAssembleia(any(AssembleiaRequestDTO.class)))
                                .thenThrow(new IllegalArgumentException("Assembleia inválida"));

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/assembleias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica o status e a mensagem de erro
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Assembleia inválida"));

                // Verifica se o Service foi chamado
                verify(contextoService)
                                .criarAssembleia(any(AssembleiaRequestDTO.class));
        }

        // Testes de criação de centro acadêmico

        @Test
        void deveCriarCentroAcademicoComSucesso() throws Exception {

                // Prepara os dados da requisição
                CentroAcademicoRequestDTO requestDTO = new CentroAcademicoRequestDTO(3L, "CA1", 30);

                // Prepara a resposta que o Service deverá retornar
                CentroAcademico responseDTO = new CentroAcademico("CA1", 30);

                // Simula o comportamento do Service
                when(contextoService.criarCentroAcademico(
                                any(CentroAcademicoRequestDTO.class)))
                                .thenReturn(responseDTO);

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/centros-academicos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica o status HTTP 201
                                .andExpect(status().isCreated());

                // Verifica se o Service foi chamado
                verify(contextoService)
                                .criarCentroAcademico(any(CentroAcademicoRequestDTO.class));
        }

        @Test
        void deveRetornarBadRequestQuandoCentroAcademicoForInvalido() throws Exception {

                // Prepara uma requisição inválida
                CentroAcademicoRequestDTO requestDTO = new CentroAcademicoRequestDTO(3L, null, 30);

                // Simula o Service lançando uma exceção
                when(contextoService.criarCentroAcademico(
                                any(CentroAcademicoRequestDTO.class)))
                                .thenThrow(new IllegalArgumentException("Centro acadêmico inválido"));

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/centros-academicos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica o status e a mensagem de erro
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Centro acadêmico inválido"));

                // Verifica se o Service foi chamado
                verify(contextoService)
                                .criarCentroAcademico(any(CentroAcademicoRequestDTO.class));
        }

        // Testes de criação de condominio

        @Test
        void deveCriarCondominioComSucesso() throws Exception {

                // Prepara os dados da requisição
                CondominioRequestDTO requestDTO = new CondominioRequestDTO(3L, "Condominio1", 30);

                // Prepara a resposta que o Service deverá retornar
                Condominio responseDTO = new Condominio("Condominio1", 30);

                // Simula o comportamento do Service
                when(contextoService.criarCondominio(any(CondominioRequestDTO.class)))
                                .thenReturn(responseDTO);

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/condominios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica o status HTTP 201
                                .andExpect(status().isCreated());

                // Verifica se o Service foi chamado
                verify(contextoService)
                                .criarCondominio(any(CondominioRequestDTO.class));
        }

        @Test
        void deveRetornarBadRequestQuandoUsuarioNaoExistir() throws Exception {

                // Prepara uma requisição com um ID de usuário inexistente
                CondominioRequestDTO requestDTO = new CondominioRequestDTO(999L, "Condominio1", 30);

                // Simula o Service informando que o usuário não foi encontrado
                when(contextoService.criarCondominio(
                                argThat(dto -> dto.id().equals(999L))))
                                .thenThrow(new IllegalArgumentException("Usuário não encontrado"));

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/condominios")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica o status e a mensagem de erro
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Usuário não encontrado"));

                // Verifica se o Service foi chamado com o ID informado
                verify(contextoService).criarCondominio(
                                argThat(dto -> dto.id().equals(999L)));
        }

        // Testes de entrar em contexto

        @Test
        void deveEntrarEmContextoComSucesso() throws Exception {

                // Prepara os dados da requisição
                UUID codigo = UUID.randomUUID();

                EntrarContextoRequestDTO requestDTO = new EntrarContextoRequestDTO(codigo.toString(), 3L);

                // Prepara a resposta que o Service deverá retornar
                ContextoResponseDTO responseDTO = new ContextoResponseDTO(
                                4L,
                                "CondominioX",
                                "CONDOMINIO",
                                "MEMBRO",
                                new Condominio("CondominioX", 10));

                // Simula o comportamento do Service
                when(contextoService.entrarContexto(3L, codigo))
                                .thenReturn(responseDTO);

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/entrar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica o status HTTP 201
                                .andExpect(status().isCreated());

                // Verifica se o Service foi chamado com os parâmetros corretos
                verify(contextoService).entrarContexto(3L, codigo);
        }

        @Test
        void deveRetornarBadRequestQuandoContextoNaoExistir() throws Exception {

                // Prepara um UUID válido
                UUID codigo = UUID.fromString(
                                "550e8400-e29b-41d4-a716-446655440000");

                EntrarContextoRequestDTO requestDTO = new EntrarContextoRequestDTO(codigo.toString(), 3L);

                // Simula o Service informando que o contexto não foi encontrado
                when(contextoService.entrarContexto(3L, codigo))
                                .thenThrow(new IllegalArgumentException("Contexto não encontrado"));

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/entrar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica o status e a mensagem de erro
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Contexto não encontrado"));

                // Verifica se o Service foi chamado com os parâmetros corretos
                verify(contextoService).entrarContexto(3L, codigo);
        }

        @Test
        void deveRetornarBadRequestQuandoCodigoForInvalido() throws Exception {

                // Prepara um código que não representa um UUID válido
                EntrarContextoRequestDTO requestDTO = new EntrarContextoRequestDTO("codigo-invalido", 3L);

                // Envia a requisição POST
                mockMvc.perform(post("/api/contextos/entrar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(requestDTO)))

                                // Verifica se o Controller retorna HTTP 400
                                .andExpect(status().isBadRequest());

                // O Service não deve ser chamado, pois a conversão para UUID falhou
                verify(contextoService, never())
                                .entrarContexto(anyLong(), any(UUID.class));
        }

        // Teste da listagem de contextos

        @Test
        void deveListarContextosDoUsuarioComSucesso() throws Exception {

                // Define o ID do usuário
                Long idUsuario = 10L;

                // Simula o Service retornando uma lista vazia (conforme esperado quando o
                // usuario ainda não tem nenhum contexto)
                when(contextoService.listarContextosDoUsuario(idUsuario))
                                .thenReturn(List.of());

                // Envia a requisição GET
                mockMvc.perform(get("/api/contextos/{id}", idUsuario))

                                // Verifica o status HTTP atual do Controller
                                .andExpect(status().isCreated());

                // Verifica se o Controller chamou o Service com o ID correto
                verify(contextoService).listarContextosDoUsuario(idUsuario);
        }

        @Test
        void deveRetornarBadRequestQuandoUsuarioForInvalido() throws Exception {

                // Define um ID inválido
                Long idUsuario = -1L;

                // Simula o Service lançando uma exceção
                when(contextoService.listarContextosDoUsuario(idUsuario))
                                .thenThrow(new IllegalArgumentException("Usuário inválido"));

                // Envia a requisição GET
                mockMvc.perform(get("/api/contextos/{id}", idUsuario))

                                // Verifica o status e a mensagem de erro
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Usuário inválido"));

                // Verifica se o Controller chamou o Service
                verify(contextoService).listarContextosDoUsuario(idUsuario);
        }

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
