package com.MC656.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VotacaoCadastroDTO(
        @NotNull(message = "O contexto é obrigatório")
        @Positive(message = "O contexto deve ser válido")
        Long contextoId,

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @NotNull(message = "A regra de aprovação é obrigatória")
        RegraAprovacao regraAprovacao,

        @NotNull(message = "O percentual mínimo é obrigatório")
        @DecimalMin(value = "0.0", message = "O percentual mínimo deve ser no mínimo 0")
        @DecimalMax(value = "100.0", message = "O percentual mínimo deve ser no máximo 100")
        BigDecimal percentualMinimo,

        @NotNull(message = "O quórum mínimo é obrigatório")
        @DecimalMin(value = "0.0", message = "O quórum mínimo deve ser no mínimo 0")
        @DecimalMax(value = "100.0", message = "O quórum mínimo deve ser no máximo 100")
        BigDecimal quorumMinimo,

        @NotNull(message = "A data de início é obrigatória")
        LocalDateTime dataInicio,

        @NotNull(message = "A data de fim é obrigatória")
        LocalDateTime dataFim,

        @NotNull(message = "O status é obrigatório")
        StatusVotacao status
) {
    public VotacaoCadastroDTO {
        if (dataInicio != null && dataFim != null && !dataFim.isAfter(dataInicio)) {
            throw new IllegalArgumentException("A data fim deve ser posterior à data início.");
        }
    }
}
