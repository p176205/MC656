package com.MC656.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/* Processo de votacao dentro de um Contexto (1 Contexto : N Votacoes).
   Por enquanto representa uma deliberacao: uma pauta votada com Sim/Nao/Abstencao. */
@Entity
@Table(name = "votacoes")
public class Votacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    // Toda votacao pertence a exatamente um contexto (define eleitorado e regra de quorum)
    @ManyToOne(optional = false)
    @JoinColumn(name = "contexto_id", nullable = false)
    private Contexto contexto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVotacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegraAprovacao regraAprovacao;

    // So eh usado quando a regra eh PERCENTUAL_MINIMO (valor entre 0 e 100)
    private Double percentualMinimo;

    // Participacao minima do eleitorado (0 a 100) para o resultado ser valido; 0 = sem quorum
    @Column(nullable = false)
    private Double quorumMinimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVotacao status;

    // Ambas opcionais: sem inicio, abre na criacao; sem fim, so encerra manualmente
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    protected Votacao() {
        // precisa pro JPA
    }

    public Votacao(String titulo, String descricao, Contexto contexto, RegraAprovacao regraAprovacao,
            Double percentualMinimo, Double quorumMinimo, LocalDateTime dataInicio,
            LocalDateTime dataFim) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.contexto = contexto;
        this.tipo = TipoVotacao.DELIBERACAO;
        this.regraAprovacao = regraAprovacao;
        this.percentualMinimo = regraAprovacao == RegraAprovacao.PERCENTUAL_MINIMO ? percentualMinimo : null;
        this.quorumMinimo = quorumMinimo;
        this.status = StatusVotacao.ABERTA;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        validar_votacao(percentualMinimo);
    }

    private void validar_votacao(Double percentualInformado) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título da votação não pode ser vazio.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição da votação não pode ser vazia.");
        }
        if (contexto == null) {
            throw new IllegalArgumentException("A votação deve pertencer a um contexto.");
        }
        if (regraAprovacao == null) {
            throw new IllegalArgumentException("A regra de aprovação deve ser informada.");
        }
        if (regraAprovacao == RegraAprovacao.PERCENTUAL_MINIMO
                && (percentualInformado == null || percentualInformado <= 0 || percentualInformado > 100)) {
            throw new IllegalArgumentException("O percentual mínimo deve estar entre 0 (exclusivo) e 100.");
        }
        if (quorumMinimo == null || quorumMinimo < 0 || quorumMinimo > 100) {
            throw new IllegalArgumentException("O quórum mínimo deve estar entre 0 e 100.");
        }
        if (dataInicio != null && dataFim != null && !dataFim.isAfter(dataInicio)) {
            throw new IllegalArgumentException("A data de fim deve ser posterior à data de início.");
        }
    }

    // A votacao recebe votos se esta ABERTA e "agora" esta dentro da janela [dataInicio, dataFim)
    public boolean aceitaVotos(LocalDateTime agora) {
        if (status != StatusVotacao.ABERTA) {
            return false;
        }
        if (dataInicio != null && agora.isBefore(dataInicio)) {
            return false;
        }
        return dataFim == null || agora.isBefore(dataFim);
    }

    // Verdadeiro se o prazo acabou mas o status ainda nao foi atualizado
    public boolean prazoExpirado(LocalDateTime agora) {
        return status == StatusVotacao.ABERTA && dataFim != null && !agora.isBefore(dataFim);
    }

    public void encerrar() {
        if (status == StatusVotacao.ENCERRADA) {
            throw new IllegalStateException("A votação já está encerrada.");
        }
        this.status = StatusVotacao.ENCERRADA;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Contexto getContexto() {
        return contexto;
    }

    public TipoVotacao getTipo() {
        return tipo;
    }

    public RegraAprovacao getRegraAprovacao() {
        return regraAprovacao;
    }

    public Double getPercentualMinimo() {
        return percentualMinimo;
    }

    public Double getQuorumMinimo() {
        return quorumMinimo;
    }

    public StatusVotacao getStatus() {
        return status;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }
}
