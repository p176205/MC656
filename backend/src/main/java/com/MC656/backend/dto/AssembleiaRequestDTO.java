package com.MC656.backend.dto;

import com.MC656.backend.entity.Papel;
import com.MC656.backend.entity.TipoContexto;

public record AssembleiaRequestDTO(
    String nome,
    int participantes
){}