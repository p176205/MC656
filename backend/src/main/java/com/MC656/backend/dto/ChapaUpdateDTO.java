package com.MC656.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChapaUpdateDTO(
        @NotNull(message = "O usuário que está editando a chapa é obrigatório")
        Long usuarioId,

        @NotBlank(message = "O nome da chapa é obrigatório")
        String nome,

        String proposta
) {}

