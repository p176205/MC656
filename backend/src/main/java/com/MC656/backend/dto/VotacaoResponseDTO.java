package com.MC656.backend.dto;

import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;
import com.MC656.backend.entity.TipoVotacao;

import java.time.LocalDateTime;

public record VotacaoResponseDTO(
        Long id,
        String titulo,
        String descricao,
        Long contextoId,
        String contextoNome,
        TipoVotacao tipo,
        RegraAprovacao regraAprovacao,
        Double percentualMinimo,
        Double quorumMinimo,
        StatusVotacao status,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {}
