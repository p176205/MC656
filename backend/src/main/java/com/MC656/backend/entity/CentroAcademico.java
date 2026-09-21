package com.MC656.backend.entity;

import jakarta.persistence.Entity;

// "CentroAcademico" deve herdar de "Contexto"
@Entity // A classe representa uma entidade no banco de dados
public class CentroAcademico extends Contexto {

    private final int tempo_vigencia;

    // Construtor
    public CentroAcademico(String nome, int tempo_vigencia){
        super(nome, TipoContexto.CENTRO_ACADEMICO);
        this.tempo_vigencia = tempo_vigencia;
    }

    // Construtor sem argumentos, para ser utilizado pelo banco de dados
    protected CentroAcademico(){}

    public int getTempoVigencia(){
        return tempo_vigencia;
    }
}
