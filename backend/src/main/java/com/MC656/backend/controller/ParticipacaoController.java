package com.MC656.backend.controller;

import com.MC656.backend.dto.ContextoParticipacaoDTO;
import com.MC656.backend.entity.Contexto;
import com.MC656.backend.entity.Participacao;
import com.MC656.backend.repository.ParticipacaoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
/*transforma os codigos de Participacao e participacaorepository para que possam ser chamados pelo front  */
@RestController
public class ParticipacaoController {

    private final ParticipacaoRepository participacaoRepository;

    public ParticipacaoController(ParticipacaoRepository participacaoRepository) {
        this.participacaoRepository = participacaoRepository;
    }

    @GetMapping("/api/usuarios/{id}/contextos")
    public List<ContextoParticipacaoDTO> listarContextosDoUsuario(@PathVariable Long id) {
        return participacaoRepository.findByUsuarioId(id).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ContextoParticipacaoDTO toDTO(Participacao participacao) {
    Contexto contexto = participacao.getContexto();
    return new ContextoParticipacaoDTO(
            contexto.getId(),
            contexto.getTipo().name(),   
            contexto.getNome(),
            participacao.getPapel()
    );
}
}