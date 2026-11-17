package com.MC656.backend.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* Representa uma chapa concorrendo em uma Votacao do tipo ELEICAO.
   Criada pelo administrador do contexto com nome e membros integrantes;
   posteriormente os proprios membros da chapa podem editar o nome e a proposta/descricao. */
@Entity
@Table(name = "chapas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"votacao_id", "nome"})
})
public class Chapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "votacao_id", nullable = false)
    private Votacao votacao;

    @Column(nullable = false)
    private String nome;

    @Column(length = 2000)
    private String proposta;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "chapa_membros",
            joinColumns = @JoinColumn(name = "chapa_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> membros = new ArrayList<>();

    protected Chapa() {
        // precisa pro JPA
    }

    public Chapa(Votacao votacao, String nome, String proposta, List<Usuario> membros) {
        if (votacao == null) {
            throw new IllegalArgumentException("A chapa deve pertencer a uma votação.");
        }
        if (votacao.getTipo() != TipoVotacao.ELEICAO) {
            throw new IllegalArgumentException("Chapas só podem ser cadastradas em votações do tipo ELEICAO.");
        }
        validarNome(nome);
        if (membros == null || membros.isEmpty()) {
            throw new IllegalArgumentException("A chapa deve possuir ao menos um membro integrante.");
        }
        this.votacao = votacao;
        this.nome = nome.trim();
        this.proposta = proposta;
        this.membros = new ArrayList<>(membros);
    }

    public boolean contemMembro(Long usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return membros.stream()
                .anyMatch(u -> u.getId() != null && u.getId().equals(usuarioId));
    }

    public void atualizarInformacoes(String novoNome, String novaProposta) {
        validarNome(novoNome);
        this.nome = novoNome.trim();
        this.proposta = novaProposta;
    }

    private void validarNome(String nomeInformado) {
        if (nomeInformado == null || nomeInformado.isBlank()) {
            throw new IllegalArgumentException("O nome da chapa não pode ser vazio.");
        }
    }

    public Long getId() {
        return id;
    }

    public Votacao getVotacao() {
        return votacao;
    }

    public String getNome() {
        return nome;
    }

    public String getProposta() {
        return proposta;
    }

    public List<Usuario> getMembros() {
        return Collections.unmodifiableList(membros);
    }
}

