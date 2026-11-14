package com.MC656.backend.dto;

import com.MC656.backend.entity.Mensagem;
import java.time.LocalDateTime;

public class MensagemResponseDTO {

    private final Long id;
    private final String texto;
    private final LocalDateTime dataEnvio;

    public MensagemResponseDTO(Mensagem mensagem){
        this.id = mensagem.getId();
        this.texto = mensagem.getTexto();
        this.dataEnvio = mensagem.getDataEnvio();
    }

    public Long getId(){
        return id;
    }

    public String getTexto(){
        return texto;
    }

    public LocalDateTime getDataEnvio(){
        return dataEnvio;
    }
}