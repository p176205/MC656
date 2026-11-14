package com.MC656.backend.controller;

import com.MC656.backend.dto.MensagemRequestDTO;
import com.MC656.backend.dto.MensagemResponseDTO;
import com.MC656.backend.entity.Mensagem;
import com.MC656.backend.service.MensagemService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contextos/{contextoId}/mensagens")
@CrossOrigin(origins = "http://localhost:3000")
public class MensagemController {

    private final MensagemService mensagemService;

    // Construtor
    public MensagemController(MensagemService mensagemService){
        this.mensagemService = mensagemService;
    }

    // Busca as mensagens de um contexto
    @GetMapping
    public ResponseEntity<?> buscarMensagens(@PathVariable Long contextoId, @RequestParam Long usuarioId){
        try {
            List<MensagemResponseDTO> mensagens = mensagemService
                    .buscarMensagens(usuarioId, contextoId)
                    .stream()
                    .map(MensagemResponseDTO::new)
                    .toList();
            return ResponseEntity.ok(mensagens);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Envia uma mensagem para um contexto
    @PostMapping
    public ResponseEntity<?> enviarMensagem(@PathVariable Long contextoId, @RequestParam Long usuarioId, @RequestBody MensagemRequestDTO dto){
        try {
            Mensagem mensagem = mensagemService.enviarMensagem(usuarioId, contextoId, dto.getTexto());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MensagemResponseDTO(mensagem));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}