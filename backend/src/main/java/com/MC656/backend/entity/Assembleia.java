package com.MC656.backend.entity;

import jakarta.persistence.Entity;

// "Assembleia" deve herdar de "Contexto"
@Entity // A classe representa uma entidade no banco de dados
public class Assembleia extends Contexto {
  
    // Construtor
    public Assembleia(String nome){
        super(nome, TipoContexto.ASSEMBLEIA);
    }

    // Construtor sem argumentos, para ser utilizado pelo banco de dados
    protected Assembleia(){}
}
