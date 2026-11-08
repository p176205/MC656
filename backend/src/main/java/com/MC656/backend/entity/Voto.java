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
    private OpcaoVoto opcao;

    @ManyToOne
    @JoinColumn(name = "chapa_id")
    private Chapa chapa;

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
        if (votacao.getTipo() == TipoVotacao.ELEICAO && opcao != OpcaoVoto.ABSTENCAO) {
            throw new IllegalArgumentException("Em eleições, a única opção sem chapa permitida é ABSTENCAO.");
        }
        if (peso == null || peso <= 0) {
            throw new IllegalArgumentException("O peso do voto deve ser maior que zero.");
        }
        this.votacao = votacao;
        this.opcao = opcao;
        this.chapa = null;
        this.peso = peso;
    }

    public Voto(Votacao votacao, Double peso, Chapa chapa) {
        if (votacao == null) {
            throw new IllegalArgumentException("O voto deve pertencer a uma votação.");
        }
        if (votacao.getTipo() != TipoVotacao.ELEICAO) {
            throw new IllegalArgumentException("Voto em chapa só é permitido em votações do tipo ELEICAO.");
        }
        if (chapa == null) {
            throw new IllegalArgumentException("A chapa de voto deve ser informada.");
        }
        if (chapa.getVotacao() != votacao
                && (chapa.getVotacao().getId() == null || !chapa.getVotacao().getId().equals(votacao.getId()))) {
            throw new IllegalArgumentException("A chapa informada não pertence a esta votação.");
        }
        if (peso == null || peso <= 0) {
            throw new IllegalArgumentException("O peso do voto deve ser maior que zero.");
        }
        this.votacao = votacao;
        this.chapa = chapa;
        this.opcao = null;
        this.peso = peso;
    }

    /* Padrão Factory Method: métodos estáticos semânticos para instanciar votos
       de acordo com a modalidade (opção de deliberação, chapa eleitoral ou voto em branco). */
    public static Voto emOpcao(Votacao votacao, OpcaoVoto opcao, Double peso) {
        return new Voto(votacao, opcao, peso);
    }

    public static Voto emChapa(Votacao votacao, Chapa chapa, Double peso) {
        return new Voto(votacao, peso, chapa);
    }

    public static Voto emBranco(Votacao votacao, Double peso) {
        return new Voto(votacao, OpcaoVoto.ABSTENCAO, peso);
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

    public Chapa getChapa() {
        return chapa;
    }

    public Double getPeso() {
        return peso;
    }
}
