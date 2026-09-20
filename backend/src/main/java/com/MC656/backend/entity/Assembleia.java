package com.MC656.backend.entity;

// "Assembleia" deve herdar de "Contexto"
public class Assembleia extends Contexto {
  
    // Construtor
    public Assembleia(String nome){
        super(nome, TipoContexto.ASSEMBLEIA);
    }
}
