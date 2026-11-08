package com.MC656.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.MC656.backend.dto.ContextoResponseDTO;
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.repository.ContextoRepository;

@RestController
@RequestMapping("/api/contextos")
public class ContextoController {

    private final ContextoRepository contextoRepository;

    public ContextoController(ContextoRepository contextoRepository) {
        this.contextoRepository = contextoRepository;
    }

    @GetMapping("/{contextoId}")
    public ResponseEntity<ContextoResponseDTO> buscarPorId(@PathVariable Long contextoId) {
        return contextoRepository.findById(contextoId)
                .map(this::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    private ContextoResponseDTO toResponseDTO(Contexto contexto) {
        return new ContextoResponseDTO(contexto.getId(), contexto.getNome(), contexto.getTipo());
    }
}
