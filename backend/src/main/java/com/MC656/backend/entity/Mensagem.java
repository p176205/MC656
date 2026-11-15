package com.MC656.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Entidade que permite o envio de mensagens anônimas dentro de contextos

@Entity
@Table(name = "mensagens")
public class Mensagem {

    // Identificador da mensagem no banco de dados
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contexto ao qual a mensagem pertence
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contexto_id", nullable = false)
    private Contexto contexto;

    // Usuário que enviou a mensagem
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    // Texto e data da mensagem

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    // Construtor para o banco de dados
    protected Mensagem() {
    }

    // Construtor geral (com argumentos)
    public Mensagem(Contexto contexto, Long usuarioId, String texto) {

        // OBS: Como nenhum dos atributos da mensagem é modificável, as verificações de
        // erros serão feitas diretamente no construtor
        if (contexto == null) {
            throw new IllegalArgumentException(
                    "A mensagem deve estar associada a um contexto.");
        }

        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "A mensagem deve estar associada a um usuário.");
        }

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "O texto da mensagem não pode ser vazio.");
        }

        this.contexto = contexto;
        this.usuarioId = usuarioId;
        this.texto = texto;
        this.dataEnvio = LocalDateTime.now();
    }

    // Conferência que a data foi salva corretamente antes do salvamento no banco de
    // dados
    @PrePersist
    protected void prePersist() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }

    // Métodos de obtenção de cada atributo da classe

    public Long getId() {
        return id;
    }

    public Contexto getContexto() {
        return contexto;
    }

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    // Obtem o id do usuario
    public Long getUsuarioId() {
        return usuarioId;
    }
}
