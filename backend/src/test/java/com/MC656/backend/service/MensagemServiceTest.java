package com.MC656.backend.service;

// Bibliotecas para os testes automáticos:

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// Importações de dentro do projeto:
import com.MC656.backend.entity.*;
import com.MC656.backend.repository.*;

// Bibliotecas necessárias de java:
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MensagemServiceTest {

    // Simulações dos repositórios que são utilizados por MensagemService:

    @Mock
    private MensagemRepository mensagemRepository;

    @Mock
    private ContextoRepository contextoRepository;

    @Mock
    private ParticipacaoRepository participacaoRepository;

    // Criação do elemento "MensagemService" utilizando os repositórios simulados:

    @InjectMocks
    private MensagemService mensagemService;

    // TESTES 1: TESTES RELACIONADOS À FUNÇÃO BUSCAR MENSAGENS
    
    // TESTE 1.1: Caso o usuário faça parte do contexto e existam mensagens naquele contexto, é possível buscá-las
    @Test
    void permiteBuscarMensagensSeUsuarioParticipaContexto(){

        // Criação de IDs de um usuário e um contexto existentes
        Long usuarioId = 1L;
        Long contextoId = 2L;

        // Simulação da participação do usuário no contexto
        List<Participacao> participacoes = List.of(mock(Participacao.class));

        // Simulação da existência de mensagens no contexto
        List<Mensagem> mensagens = List.of(mock(Mensagem.class), mock(Mensagem.class), mock(Mensagem.class));

        // Simulação do resultado esperado pelo método 
        when(participacaoRepository.findByUsuarioIdAndContextoId(usuarioId, contextoId)).thenReturn(participacoes);
        when(mensagemRepository.findByContextoIdOrderByDataEnvioAsc(contextoId)).thenReturn(mensagens);

        // Geração do resultado do método
        List<Mensagem> resultado = mensagemService.buscarMensagens(usuarioId, contextoId);

        // Confirma que o resultado obtido é igual ao resultado esperado (o método funciona nesse caso):
        assertSame(mensagens, resultado);

        // Confirma que os métodos esperados realmente foram executados
        verify(participacaoRepository).findByUsuarioIdAndContextoId(usuarioId, contextoId);
        verify(mensagemRepository).findByContextoIdOrderByDataEnvioAsc(contextoId);
    }

    // TESTE 1.2: Caso o usuário faça parte do contexto, mas não existem mensagens nesse contexto, retorna uma lista vazia
    @Test
    void retornaListaVaziaSeNaoExistemMensagens(){

        // Criação de IDs de um usuário e um contexto existentes
        Long usuarioId = 1L;
        Long contextoId = 2L;

        // Simulação da participação do usuário no contexto
        when(participacaoRepository.findByUsuarioIdAndContextoId(usuarioId, contextoId)).thenReturn(List.of(mock(Participacao.class)));

        // Simulação de um contexto sem mensagens
        when(mensagemRepository.findByContextoIdOrderByDataEnvioAsc(contextoId)).thenReturn(List.of());

        // Geração do resultado do método
        List<Mensagem> resultado = mensagemService.buscarMensagens(usuarioId, contextoId);

        // Confirma que o resultado obtido é vazio
        assertTrue(resultado.isEmpty());

        // Confirma que o método esperado realmente foi executado
        verify(mensagemRepository).findByContextoIdOrderByDataEnvioAsc(contextoId);
    }

    // TESTE 1.3: Caso o usuário não faça parte do contexto, não é possível buscar mensagens
    @Test
    void naoPermiteBuscarMensagensSeUsuarioNaoParticipaContexto(){

        // Criação de IDs de um usuário e um contexto existentes
        Long usuarioId = 1L;
        Long contextoId = 2L;

        // Simulação de uma lista de participação vazia no contexto (o usuário não faz parte do contexto)
        when(participacaoRepository.findByUsuarioIdAndContextoId(usuarioId, contextoId)).thenReturn(List.of());

        // Verifica que o método retorna a exceção esperada caso um usuário que não faça parte do contexto tente buscar mensagens
        IllegalArgumentException excecao = assertThrows(IllegalArgumentException.class, () -> mensagemService.buscarMensagens(usuarioId, contextoId));
        assertEquals("O usuário não faz parte deste contexto.", excecao.getMessage());

        // Verifica que o método bloqueado realmente não foi executado
        verify(mensagemRepository, never()).findByContextoIdOrderByDataEnvioAsc(anyLong());
    }

}
