package com.MC656.backend.model;

import jakarta.persistence.*;

@Entity 
@Table (name = "usuarios")
public class Usuario {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    
    public Usuario() {
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
