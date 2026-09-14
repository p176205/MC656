package com.MC656.backend.entity;

import jakarta.persistence.*;

@Entity // Diz que essa classe representa  uma entidade do nosso banco de dados
@Table (name = "usuarios") // Diz a qual tabela corresponde
public class Usuario {

    @Id  // Diz para o banco gerar um id automaticamente
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    
    public Usuario(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

}
