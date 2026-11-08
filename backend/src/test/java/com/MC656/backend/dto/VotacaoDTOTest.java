package com.MC656.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class VotacaoDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final LocalDateTime dataInicio = LocalDateTime.of(2026, 11, 1, 10, 0);
    private final LocalDateTime dataFim = LocalDateTime.of(2026, 11, 2, 10, 0);

    @Test
    void deveCriarDTODeCadastroComDadosValidos() {
        VotacaoCadastroDTO dto = criarCadastro(1L, "Votação", "Descrição", dataInicio, dataFim);

        assertTrue(validator.validate(dto).isEmpty());
        assertEquals(1L, dto.contextoId());
        assertEquals(RegraAprovacao.MAIORIA_SIMPLES, dto.regraAprovacao());
        assertEquals(StatusVotacao.AGENDADA, dto.status());
    }

    @Test
    void deveValidarCamposObrigatoriosDoCadastro() {
        VotacaoCadastroDTO dto = new VotacaoCadastroDTO(
                null, " ", " ", null, null, null, null, null, null);

        Set<ConstraintViolation<VotacaoCadastroDTO>> violacoes = validator.validate(dto);

        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("contextoId")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("nome")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("descricao")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("dataInicio")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("dataFim")));
    }

    @Test
    void deveRejeitarPercentuaisForaDosLimites() {
        VotacaoCadastroDTO dto = new VotacaoCadastroDTO(
                1L, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                new BigDecimal("-0.01"), new BigDecimal("100.01"),
                dataInicio, dataFim, StatusVotacao.AGENDADA);

        Set<ConstraintViolation<VotacaoCadastroDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.stream().anyMatch(violacao ->
                violacao.getPropertyPath().toString().equals("percentualMinimo")));
        assertTrue(violacoes.stream().anyMatch(violacao ->
                violacao.getPropertyPath().toString().equals("quorumMinimo")));
    }

    @Test
    void deveExigirDataFimPosteriorADataInicio() {
        assertThrows(IllegalArgumentException.class,
                () -> criarCadastro(1L, "Votação", "Descrição", dataInicio, dataInicio));
        assertThrows(IllegalArgumentException.class,
                () -> criarCadastro(1L, "Votação", "Descrição", dataFim, dataInicio));
    }

    @Test
    void respostaDeveExporCamposComNomesDoContrato() {
        VotacaoResponseDTO dto = new VotacaoResponseDTO(
                10L, 1L, "Votação", "Descrição", RegraAprovacao.MAIORIA_SIMPLES,
                new BigDecimal("50"), new BigDecimal("25"),
                dataInicio, dataFim, StatusVotacao.AGENDADA);

        assertEquals(10L, dto.id());
        assertEquals(1L, dto.contextoId());
        assertEquals("Votação", dto.nome());
        assertEquals(new BigDecimal("25"), dto.quorumMinimo());
        assertEquals(dataInicio, dto.dataInicio());
        assertEquals(dataFim, dto.dataFim());
    }

    private VotacaoCadastroDTO criarCadastro(Long contextoId, String nome, String descricao,
            LocalDateTime dataInicio, LocalDateTime dataFim) {
        return new VotacaoCadastroDTO(contextoId, nome, descricao, RegraAprovacao.MAIORIA_SIMPLES,
                new BigDecimal("50"), new BigDecimal("25"), dataInicio, dataFim, StatusVotacao.AGENDADA);
    }
}
