package com.MC656.backend.entity;

/* Tipo do processo de votacao:
   - DELIBERACAO: pauta votada com SIM, NAO ou ABSTENCAO
   - ELEICAO: escolha entre chapas cadastradas (ou ABSTENCAO / voto em branco) */
public enum TipoVotacao {
    DELIBERACAO,
    ELEICAO
}
