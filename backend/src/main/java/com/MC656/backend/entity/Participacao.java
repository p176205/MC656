package com.MC656.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

/*Entidade associativa para a relacao (N:N) entre Usuario e Contexto que carrega
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

    /* Peso do voto deste usuario NESTE contexto (ex.: fracao ideal da unidade no condominio).
       Fica aqui, e não em Usuario ou Contexto, porque varia por usuario e por contexto. */
    @Column(nullable = false)
    @ColumnDefault("1.0")
    private Double peso = 1.0;

    protected Participacao() {
        // precisa pro JPA
    }

    public Participacao(Usuario usuario, Contexto contexto, Papel papel) {
        this(usuario, contexto, papel, 1.0);
    }

    public Participacao(Usuario usuario, Contexto contexto, Papel papel, Double peso) {
        if (peso == null || peso <= 0) {
            throw new IllegalArgumentException("O peso da participação deve ser maior que zero.");
        }
        this.usuario = usuario;
        this.contexto = contexto;
        this.papel = papel;
        this.peso = peso;
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

    public Double getPeso() {
        return peso;
    }
}