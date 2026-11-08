package com.MC656.backend.entity;

import jakarta.persistence.*;

/* Urna anonima: guarda so a opcao e o peso do voto. Nao tem referencia ao usuario,
   para que nao seja possivel associar o voto a quem votou. O controle de quem ja votou
   fica separado em RegistroVotante. */
@Entity
@Table(name = "votos")
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "votacao_id", nullable = false)
    private Votacao votacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpcaoVoto opcao;

    @Column(nullable = false)
    private Double peso;

    protected Voto() {
        // precisa pro JPA
    }

    public Voto(Votacao votacao, OpcaoVoto opcao, Double peso) {
        if (votacao == null) {
            throw new IllegalArgumentException("O voto deve pertencer a uma votação.");
        }
        if (opcao == null) {
            throw new IllegalArgumentException("A opção de voto deve ser informada.");
        }
        if (peso == null || peso <= 0) {
            throw new IllegalArgumentException("O peso do voto deve ser maior que zero.");
        }
        this.votacao = votacao;
        this.opcao = opcao;
        this.peso = peso;
    }

    public Long getId() {
        return id;
    }

    public Votacao getVotacao() {
        return votacao;
    }

    public OpcaoVoto getOpcao() {
        return opcao;
    }

    public Double getPeso() {
        return peso;
    }
}
