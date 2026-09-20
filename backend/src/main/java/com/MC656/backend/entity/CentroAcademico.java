package com.MC656.backend.entity;

import jakarta.persistence.Entity;

// "CentroAcademico" deve herdar de "Contexto"
@Entity // A classe representa uma entidade no banco de dados
public class CentroAcademico extends Contexto {

    // Construtor
    public CentroAcademico(String nome){
        super(nome, TipoContexto.CENTRO_ACADEMICO);
    }

    // Construtor sem argumentos, para ser utilizado pelo banco de dados
    protected CentroAcademico(){}
}
