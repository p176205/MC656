package com.MC656.backend.dto;

import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.StatusVotacao;
import com.MC656.backend.entity.TipoVotacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime dataFim,
        String pauta,
        Integer participantesAptos,
        BigDecimal pesoVoto,
        String unidadeApartamento,
        String categoriaSegmento,
        String elegibilidade,
        List<ChapaResponseDTO> chapas
) {}
