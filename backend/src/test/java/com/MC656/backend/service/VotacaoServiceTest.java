package com.MC656.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.MC656.backend.dto.VotacaoCadastroDTO;
import com.MC656.backend.dto.VotacaoResponseDTO;
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;
import com.MC656.backend.entity.Votacao;
import com.MC656.backend.repository.ContextoRepository;
import com.MC656.backend.repository.VotacaoRepository;

@ExtendWith(MockitoExtension.class)
class VotacaoServiceTest {

    @Mock
    private VotacaoRepository votacaoRepository;

    @Mock
    private ContextoRepository contextoRepository;

    @InjectMocks
    private VotacaoService votacaoService;

    @Test
    void devePersistirVotacaoAssociadaAoContextoERetornarDadosCriados() {
        Contexto contexto = org.mockito.Mockito.mock(Contexto.class);
        when(contexto.getId()).thenReturn(42L);
        when(contextoRepository.findById(42L)).thenReturn(Optional.of(contexto));
        when(votacaoRepository.save(any(Votacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        VotacaoCadastroDTO dto = criarCadastro(42L);

        VotacaoResponseDTO resposta = votacaoService.cadastrar(dto);

        ArgumentCaptor<Votacao> captor = ArgumentCaptor.forClass(Votacao.class);
        verify(votacaoRepository).save(captor.capture());
        Votacao salva = captor.getValue();
        assertEquals(contexto, salva.getContexto());
        assertEquals("Eleição de representante", salva.getNome());
        assertEquals(new BigDecimal("60.5"), salva.getPercentualMinimo());
        assertEquals(new BigDecimal("25.25"), salva.getQuorumMinimo());
        assertEquals(LocalDateTime.of(2026, 11, 1, 10, 0), salva.getDataInicio());
        assertEquals(LocalDateTime.of(2026, 11, 2, 10, 0), salva.getDataFim());
        assertEquals(42L, resposta.contextoId());
        assertEquals(salva.getNome(), resposta.nome());
        assertEquals(salva.getRegraAprovacao(), resposta.regraAprovacao());
        assertEquals(salva.getPercentualMinimo(), resposta.percentualMinimo());
        assertEquals(salva.getQuorumMinimo(), resposta.quorumMinimo());
        assertEquals(salva.getDataInicio(), resposta.dataInicio());
        assertEquals(salva.getDataFim(), resposta.dataFim());
        assertEquals(salva.getStatus(), resposta.status());
    }

    @Test
    void deveInformar404QuandoContextoDoCadastroNaoExiste() {
        when(contextoRepository.findById(42L)).thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> votacaoService.cadastrar(criarCadastro(42L)));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verifyNoInteractions(votacaoRepository);
    }

    private VotacaoCadastroDTO criarCadastro(Long contextoId) {
        return new VotacaoCadastroDTO(
                contextoId,
                "Eleição de representante",
                "Escolha do representante.",
                RegraAprovacao.MAIORIA_SIMPLES,
                new BigDecimal("60.5"),
                new BigDecimal("25.25"),
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 2, 10, 0),
                StatusVotacao.AGENDADA);
    }
}
