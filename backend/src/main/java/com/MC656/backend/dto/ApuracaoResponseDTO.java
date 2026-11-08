package com.MC656.backend.dto;

import com.MC656.backend.entity.RegraAprovacao;
import com.MC656.backend.entity.ResultadoVotacao;

// Resultado de uma votacao encerrada. Os totais de votos sao somas de pesos.
public record ApuracaoResponseDTO(
        Long votacaoId,
        String titulo,
        ResultadoVotacao resultado,
        boolean quorumAtingido,
        double votosSim,
        double votosNao,
        double abstencoes,
        int totalVotantes,
        RegraAprovacao regraAprovacao,
        Double percentualMinimo,
        // % de SIM sobre (SIM + NAO); null se nao houve votos SIM/NAO
        Double percentualSimObtido
) {}
