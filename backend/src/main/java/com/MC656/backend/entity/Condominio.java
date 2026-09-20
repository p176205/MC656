package com.MC656.backend.entity;

// "Condominio" deve herdar de "Contexto"
public class Condominio extends Contexto {
    
    // Construtor
    public Condominio(String nome){
        super(nome, TipoContexto.CONDOMINIO);
    }
}
