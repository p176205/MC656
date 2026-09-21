package com.MC656.backend.entity;

import jakarta.persistence.Entity;

// "CentroAcademico" deve herdar de "Contexto"
@Entity // A classe representa uma entidade no banco de dados
public class CentroAcademico extends Contexto {

    private int tempo_vigencia;

    // Construtor
    public CentroAcademico(String nome, int tempo_vigencia){
        super(nome, TipoContexto.CENTRO_ACADEMICO);
        this.tempo_vigencia = tempo_vigencia;
        validar_centro_academico();
    }

    // Construtor sem argumentos, para ser utilizado pelo banco de dados
    protected CentroAcademico(){}

    // O tempo de vigência é fixo, logo, não poderá ser alterado
    public int getTempoVigencia(){
        return tempo_vigencia;
    }

    // Confere se a entidade "CentroAcademico" poderá ser criada sem erros
    public final void validar_centro_academico(){

        // O tempo de vigência (em anos) deve ser um número inteiro maior do que 0
        if (tempo_vigencia <= 0){
            throw new IllegalArgumentException("O tempo de vigência deve ser maior que zero.");
        }
    }
}

