package com.MC656.backend.dto;

import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.TipoVotacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Dados para abrir uma pauta (DELIBERACAO) ou uma eleicao de chapas (ELEICAO).
// Faixas de valores sao validadas na entidade Votacao e no VotacaoService.
public record VotacaoCreateDTO(
        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @NotNull(message = "O contexto é obrigatório")
        Long contextoId,

        @NotNull(message = "O tipo da votação é obrigatório")
        TipoVotacao tipo,

        @NotNull(message = "A regra de aprovação é obrigatória")
        RegraAprovacao regraAprovacao,

        // Obrigatorio apenas para PERCENTUAL_MINIMO
        Double percentualMinimo,

        @NotNull(message = "O quórum mínimo é obrigatório (use 0 para não exigir quórum)")
        Double quorumMinimo,

        LocalDateTime dataInicio,
        LocalDateTime dataFim,

        String pauta,

        @Positive(message = "A quantidade de participantes aptos deve ser positiva")
        Integer participantesAptos,

        @jakarta.validation.constraints.DecimalMin(
                value = "0.0", inclusive = false, message = "O peso do voto deve ser positivo")
        BigDecimal pesoVoto,

        String unidadeApartamento,

        String categoriaSegmento,

        String elegibilidade,

        @NotNull(message = "O administrador responsável pela criação da votação é obrigatório")
        Long adminId,

        // Obrigatorio quando tipo == ELEICAO
        List<@Valid ChapaCreateDTO> chapas
) {}
