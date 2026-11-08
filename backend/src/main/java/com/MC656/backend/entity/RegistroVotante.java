package com.MC656.backend.entity;

import jakarta.persistence.*;

/* Registra QUE o usuario votou em uma votacao (nao O QUE votou).
   A constraint unica impede voto duplicado mesmo com requisicoes simultaneas. */
@Entity
@Table(name = "registros_votantes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"votacao_id", "usuario_id"}))
public class RegistroVotante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "votacao_id", nullable = false)
    private Votacao votacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    protected RegistroVotante() {
        // precisa pro JPA
    }

    public RegistroVotante(Votacao votacao, Usuario usuario) {
        this.votacao = votacao;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public Votacao getVotacao() {
        return votacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
