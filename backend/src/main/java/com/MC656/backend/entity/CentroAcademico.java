package com.MC656.backend.entity;

// "CentroAcademico" deve herdar de "Contexto"
public class CentroAcademico extends Contexto {

    // Construtor
    public CentroAcademico(String nome){
        super(nome, TipoContexto.CENTRO_ACADEMICO);
    }
}
