package com.MC656.backend.dto;

public record CondominioRequestDTO (
    Long id,
    String nome,
    int total_moradores
){}
