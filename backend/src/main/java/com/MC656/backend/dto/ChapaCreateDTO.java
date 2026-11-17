package com.MC656.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ChapaCreateDTO(
        @NotBlank(message = "O nome da chapa é obrigatório")
        String nome,

        String proposta,

        @NotEmpty(message = "A chapa deve possuir ao menos um membro integrante")
        List<Long> membrosIds
) {
    public ChapaCreateDTO(String nome, List<Long> membrosIds) {
        this(nome, null, membrosIds);
    }
}

