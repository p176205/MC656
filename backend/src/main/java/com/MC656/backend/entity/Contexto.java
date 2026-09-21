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
        validar_contexto();
    }

    // Confere se a entidade "Contexto" poderá ser criada sem erros
    public final void validar_contexto(){

        // O nome do "Contexto" não pode estar vazio
        if (nome == null || nome.isBlank()){
            throw new IllegalArgumentException("O nome do contexto não pode ser vazio.");
        }
        // O tipo do "Contexto" não pode ser nulo
        if (tipo == null){
            throw new IllegalArgumentException("O tipo do contexto deve ser informado.");
        }
    }

    public Long getId(){
        return id;
    }

    public TipoContexto getTipo(){
        return tipo;
    }

    public String getNome(){
        return nome;
    }

    // O nome é o único atributo do "Contexto" que poderá ser modificado após a criação da entidade, caso necessário:
    public void setNome(String nome){

        // O nome do "Contexto" não pode estar vazio
        if (nome == null || nome.isBlank()){
            throw new IllegalArgumentException("O nome do contexto não pode ser vazio.");
        }
        this.nome = nome;
    }
}
