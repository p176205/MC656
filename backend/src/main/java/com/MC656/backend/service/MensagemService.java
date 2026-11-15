package com.MC656.backend.service;

import com.MC656.backend.entity.*;
import com.MC656.backend.repository.*;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final ContextoRepository contextoRepository;
    private final ParticipacaoRepository participacaoRepository;

    // Construtor
    public MensagemService(MensagemRepository mensagemRepository, ContextoRepository contextoRepository, ParticipacaoRepository participacaoRepository){
        this.mensagemRepository = mensagemRepository;
        this.contextoRepository = contextoRepository;
        this.participacaoRepository = participacaoRepository;
    }

    // Verifica se o usuário faz parte do contexto
    private void verificarParticipacao(Long usuarioId, Long contextoId){
        List<Participacao> participacoes = participacaoRepository.findByUsuarioIdAndContextoId(usuarioId, contextoId);
        if (participacoes.isEmpty()) {
            throw new IllegalArgumentException("O usuário não faz parte deste contexto.");
        }
    }

    // Caso o usuário faça parte do contexto, resgata a lista de mensagens dentro do contexto
    public List<Mensagem> buscarMensagens(Long usuarioId, Long contextoId){
        verificarParticipacao(usuarioId, contextoId);
        return mensagemRepository.findByContextoIdOrderByDataEnvioAsc(contextoId);
    }

    // Caso o usuário faça parte do contexto, envia uma nova mensagem
    public Mensagem enviarMensagem(Long usuarioId, Long contextoId, String texto) {

        // Confere a participação e resgata o contexto no banco de dados
        verificarParticipacao(usuarioId, contextoId);
        Contexto contexto = contextoRepository.findById(contextoId)
            .orElseThrow(() -> new IllegalArgumentException("Contexto não encontrado."));

        // Registra a nova mensagem
        Mensagem mensagem = new Mensagem(contexto, usuarioId, texto);
        return mensagemRepository.save(mensagem);
    }
}
