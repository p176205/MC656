package com.MC656.backend.dto;

import com.MC656.backend.entity.Papel;

public record MembroDTO(
        Long id,
        String nome,
        String email,
        Double peso,
        Papel papel
) {}
