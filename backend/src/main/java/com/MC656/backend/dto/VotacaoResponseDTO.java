package com.MC656.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;

public record VotacaoResponseDTO(
        Long id,
        Long contextoId,
        String nome,
        String descricao,
        RegraAprovacao regraAprovacao,
        BigDecimal percentualMinimo,
        BigDecimal quorumMinimo,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        StatusVotacao status
) {}
