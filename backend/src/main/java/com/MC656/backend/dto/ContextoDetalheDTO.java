package com.MC656.backend.dto;

import com.MC656.backend.entity.TipoContexto;

public record ContextoDetalheDTO(Long id, String nome, TipoContexto tipo) {}
