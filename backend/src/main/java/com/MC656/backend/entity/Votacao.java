package com.MC656.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Votacao {

    private static final BigDecimal PERCENTUAL_MAXIMO = new BigDecimal("100");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "contexto_id", nullable = false)
    private Contexto contexto;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegraAprovacao regraAprovacao;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualMinimo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal quorumMinimo;

    @Column(nullable = false)
    private LocalDateTime dataInicio;

    @Column(nullable = false)
    private LocalDateTime dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVotacao status;

    private String pauta;

    private Integer participantesAptos;

    private BigDecimal pesoVoto;

    private String unidadeApartamento;

    private String categoriaSegmento;

    private String elegibilidade;

    protected Votacao() {
        // Construtor utilizado pelo JPA.
    }

    public Votacao(Contexto contexto, String nome, String descricao, RegraAprovacao regraAprovacao,
            BigDecimal percentualMinimo, BigDecimal quorumMinimo, LocalDateTime dataInicio,
            LocalDateTime dataFim, StatusVotacao status, String pauta, Integer participantesAptos,
            BigDecimal pesoVoto, String unidadeApartamento, String categoriaSegmento, String elegibilidade) {
        this.contexto = contexto;
        this.nome = nome;
        this.descricao = descricao;
        this.regraAprovacao = regraAprovacao;
        this.percentualMinimo = percentualMinimo;
        this.quorumMinimo = quorumMinimo;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.pauta = pauta;
        this.participantesAptos = participantesAptos;
        this.pesoVoto = pesoVoto;
        this.unidadeApartamento = unidadeApartamento;
        this.categoriaSegmento = categoriaSegmento;
        this.elegibilidade = elegibilidade;
        validar();
    }

    public Long getId() {
        return id;
    }

    public Contexto getContexto() {
        return contexto;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public RegraAprovacao getRegraAprovacao() {
        return regraAprovacao;
    }

    public BigDecimal getPercentualMinimo() {
        return percentualMinimo;
    }

    public BigDecimal getQuorumMinimo() {
        return quorumMinimo;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public StatusVotacao getStatus() {
        return status;
    }

    public String getPauta() {
        return pauta;
    }

    public Integer getParticipantesAptos() {
        return participantesAptos;
    }

    public BigDecimal getPesoVoto() {
        return pesoVoto;
    }

    public String getUnidadeApartamento() {
        return unidadeApartamento;
    }

    public String getCategoriaSegmento() {
        return categoriaSegmento;
    }

    public String getElegibilidade() {
        return elegibilidade;
    }

    public void setContexto(Contexto contexto) {
        validarContexto(contexto);
        validarCamposPorTipoContexto(contexto);
        this.contexto = contexto;
    }

    public void setNome(String nome) {
        validarNome(nome);
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        validarDescricao(descricao);
        this.descricao = descricao;
    }

    public void setRegraAprovacao(RegraAprovacao regraAprovacao) {
        validarRegraAprovacao(regraAprovacao);
        this.regraAprovacao = regraAprovacao;
    }

    public void setPercentualMinimo(BigDecimal percentualMinimo) {
        validarPercentual(percentualMinimo, "O percentual mínimo");
        this.percentualMinimo = percentualMinimo;
    }

    public void setQuorumMinimo(BigDecimal quorumMinimo) {
        validarPercentual(quorumMinimo, "O quórum mínimo");
        this.quorumMinimo = quorumMinimo;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        validarDatas(dataInicio, dataFim);
        this.dataInicio = dataInicio;
    }

    public void setDataFim(LocalDateTime dataFim) {
        validarDatas(dataInicio, dataFim);
        this.dataFim = dataFim;
    }

    public void setStatus(StatusVotacao status) {
        validarStatus(status);
        this.status = status;
    }

    private void validar() {
        validarContexto(contexto);
        validarNome(nome);
        validarDescricao(descricao);
        validarRegraAprovacao(regraAprovacao);
        validarPercentual(percentualMinimo, "O percentual mínimo");
        validarPercentual(quorumMinimo, "O quórum mínimo");
        validarDatas(dataInicio, dataFim);
        validarStatus(status);
        validarCamposPorTipoContexto(contexto);
    }

    private void validarContexto(Contexto contexto) {
        if (contexto == null) {
            throw new IllegalArgumentException("O contexto da votação deve ser informado.");
        }
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da votação não pode ser vazio.");
        }
    }

    private void validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição da votação não pode ser vazia.");
        }
    }

    private void validarRegraAprovacao(RegraAprovacao regraAprovacao) {
        if (regraAprovacao == null) {
            throw new IllegalArgumentException("A regra de aprovação deve ser informada.");
        }
    }

    private void validarDatas(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null || !dataFim.isAfter(dataInicio)) {
            throw new IllegalArgumentException("A data fim deve ser posterior à data início.");
        }
    }

    private void validarStatus(StatusVotacao status) {
        if (status == null) {
            throw new IllegalArgumentException("O status da votação deve ser informado.");
        }
    }

    private void validarPercentual(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0 || valor.compareTo(PERCENTUAL_MAXIMO) > 0) {
            throw new IllegalArgumentException(campo + " deve estar entre 0 e 100.");
        }
    }

    private void validarCamposPorTipoContexto(Contexto contexto) {
        if (contexto.getTipo() == null) {
            throw new IllegalArgumentException("O tipo do contexto da votação deve ser informado.");
        }
        switch (contexto.getTipo()) {
            case ASSEMBLEIA -> {
                validarTextoObrigatorio(pauta, "A pauta");
                if (participantesAptos == null || participantesAptos <= 0) {
                    throw new IllegalArgumentException("A quantidade de participantes aptos deve ser positiva.");
                }
                rejeitarCamposNaoAplicaveis(pesoVoto, unidadeApartamento, categoriaSegmento, elegibilidade);
            }
            case CONDOMINIO -> {
                if (pesoVoto == null || pesoVoto.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("O peso do voto deve ser positivo.");
                }
                validarTextoObrigatorio(unidadeApartamento, "A unidade ou apartamento");
                rejeitarCamposNaoAplicaveis(pauta, participantesAptos, categoriaSegmento, elegibilidade);
            }
            case CENTRO_ACADEMICO -> {
                validarTextoObrigatorio(categoriaSegmento, "A categoria ou segmento");
                validarTextoObrigatorio(elegibilidade, "A elegibilidade");
                rejeitarCamposNaoAplicaveis(pauta, participantesAptos, pesoVoto, unidadeApartamento);
            }
        }
    }

    private void validarTextoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " deve ser informado(a).");
        }
    }

    private void rejeitarCamposNaoAplicaveis(Object... valores) {
        for (Object valor : valores) {
            if (valor != null) {
                throw new IllegalArgumentException("A votação contém campos que não se aplicam ao tipo do contexto.");
            }
        }
    }
}
