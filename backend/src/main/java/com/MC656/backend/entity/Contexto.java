package com.MC656.backend.entity;

import jakarta.persistence.*;

@Entity // A classe representa uma entidade no banco de dados

// Define a estratégia de herança para o banco de dados
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class Contexto {
// "Contexto" deve ser uma classe abstrata, pois não deve existir um "Contexto" que não seja "CentroAcademico", "Condominio" ou "Assembleia"

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Gera um id para o banco de dados
    private Long id;

    private String nome;

    @Enumerated(EnumType.STRING) // Garante que o nome do tipo será salvo no banco de dados, ao invés de números
    private TipoContexto tipo;

    // Construtor sem argumentos (deve ser protegido, para não permitir a criação de uma entidade "Contexto" fora do mecanismo de herança/JPA)
    protected Contexto(){}

    // Construtor com argumentos (utilizado por "CentroAcademico", "Condominio" e "Assembleia")
    public Contexto(String nome, TipoContexto tipo){
        this.nome = nome;
        this.tipo = tipo;
    }

    public Long getId(){
        return id;
    }

    public String getNome(){
        return nome;
    }

    public TipoContexto getTipo(){
        return tipo;
    }
}
