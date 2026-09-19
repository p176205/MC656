package com.MC656.backend.entity;

import jakarta.persistence.*;

/*isso eh provisorio, precisa para a issue 6, mas ainda ta sendo feito pela issue 8. */
@Entity
@Table(name = "contextos")
public class Contexto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo; // Centro Acadêmico,Condomínio, Assembleia, etc
    private String nome;

    protected Contexto() {
       
    }

    public Contexto(String tipo, String nome) {
        this.tipo = tipo;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }
}