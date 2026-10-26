package com.MC656.backend.dto;

import com.MC656.backend.entity.RegraAprovacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// Dados para abrir uma pauta. Faixas de valores sao validadas na entidade Votacao.
public record VotacaoCreateDTO(
        @NotBlank(message = "O título é obrigatório")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @NotNull(message = "O contexto é obrigatório")
        Long contextoId,

        @NotNull(message = "A regra de aprovação é obrigatória")
        RegraAprovacao regraAprovacao,

        // Obrigatorio apenas para PERCENTUAL_MINIMO
        Double percentualMinimo,

        @NotNull(message = "O quórum mínimo é obrigatório (use 0 para não exigir quórum)")
        Double quorumMinimo,

        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {}
