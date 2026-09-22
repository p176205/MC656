package com.MC656.backend.entity;

import jakarta.persistence.*;

/*Entidade associativa para a relação (N:N) entre Usuario e Contexto que carrega
  um atributo proprio (papel)*/
@Entity
@Table(name = "participacoes")
public class Participacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contexto_id", nullable = false)
    private Contexto contexto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Papel papel;

    protected Participacao() {
        // precisa pro JPA
    }

    public Participacao(Usuario usuario, Contexto contexto, Papel papel) {
        this.usuario = usuario;
        this.contexto = contexto;
        this.papel = papel;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Contexto getContexto() {
        return contexto;
    }

    public Papel getPapel() {
        return papel;
    }
}