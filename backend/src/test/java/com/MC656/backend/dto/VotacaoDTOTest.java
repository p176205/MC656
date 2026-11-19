package com.MC656.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;
import com.MC656.backend.entity.TipoVotacao;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class VotacaoDTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final LocalDateTime dataInicio = LocalDateTime.of(2026, 11, 1, 10, 0);
    private final LocalDateTime dataFim = LocalDateTime.of(2026, 11, 2, 10, 0);

    @Test
    void deveValidarCamposObrigatoriosDoCadastro() {
        VotacaoCreateDTO dto = new VotacaoCreateDTO(
                " ", " ", null, null, null, null, null, null, null, null, null);

        Set<ConstraintViolation<VotacaoCreateDTO>> violacoes = validator.validate(dto);

        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("contextoId")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("tipo")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("titulo")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("descricao")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("regraAprovacao")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("quorumMinimo")));
        assertTrue(violacoes.stream().anyMatch(violacao -> violacao.getPropertyPath().toString().equals("adminId")));
    }

    @Test
    void respostaDeveExporOsCamposDoContrato() {
        VotacaoResponseDTO dto = new VotacaoResponseDTO(
                10L, "Votação", "Descrição", 1L, "Assembleia", TipoVotacao.DELIBERACAO,
                RegraAprovacao.MAIORIA_SIMPLES, null, 25.0, StatusVotacao.ABERTA,
                dataInicio, dataFim, List.of());

        assertEquals(10L, dto.id());
        assertEquals("Votação", dto.titulo());
        assertEquals(25.0, dto.quorumMinimo());
        assertEquals(dataInicio, dto.dataInicio());
        assertEquals(dataFim, dto.dataFim());
        assertTrue(dto.chapas().isEmpty());
    }
}
