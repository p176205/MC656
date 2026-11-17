package com.MC656.backend.dto;

import java.util.List;

public record ChapaResponseDTO(
        Long id,
        Long votacaoId,
        String nome,
        String proposta,
        List<MembroChapaDTO> membros
) {}

