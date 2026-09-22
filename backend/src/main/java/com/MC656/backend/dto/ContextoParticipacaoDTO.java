package com.MC656.backend.dto;

import com.MC656.backend.entity.Papel;

/*esse DTO eh usado para alimentar a tela de seleção de contextos, para nao expor a entidade Participacao inteira via API.*/
public class ContextoParticipacaoDTO {

    private final Long contextoId;
    private final String tipo;
    private final String nome;
    private final Papel papel;

    public ContextoParticipacaoDTO(Long contextoId, String tipo, String nome, Papel papel) {
        this.contextoId = contextoId;
        this.tipo = tipo;
        this.nome = nome;
        this.papel = papel;
    }

    public Long getContextoId() {
        return contextoId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }

    public Papel getPapel() {
        return papel;
    }
}