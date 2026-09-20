package com.MC656.backend.entity;

import jakarta.persistence.Entity;

// "Condominio" deve herdar de "Contexto"
@Entity // A classe representa uma entidade no banco de dados
public class Condominio extends Contexto {

    private Usuario representante;
    
    // Construtor
    public Condominio(String nome){
        super(nome, TipoContexto.CONDOMINIO);
    }

    // Construtor sem argumentos, para ser utilizado pelo banco de dados
    protected Condominio(){}

    // Obtém o representante (síndico) atual do condomínio
    public Usuario getRepresentante(){
        return representante;
    }

    // Adiciona ou modifica o representante (síndico) atual do condomínio
    public void setRepresentante(Usuario representante){
        this.representante = representante;
    }
}
