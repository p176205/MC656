package com.MC656.backend.dto;

import com.MC656.backend.entity.Contexto;

public record ContextoResponseDTO(
    Long id,
    String nome,
    String tipo,
    String papel,
    Contexto contexto
) {} // Java criará automaticamente um construtor com os cinco parâmetros, além dos métodos de acesso.
