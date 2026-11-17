package com.MC656.backend.dto;

public record ResultadoChapaDTO(
        Long chapaId,
        String nome,
        double votos,
        Double percentualVotosValidos
) {}

