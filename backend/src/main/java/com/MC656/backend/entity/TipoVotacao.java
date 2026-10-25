package com.MC656.backend.entity;

/* Tipo do processo de votacao. "Votacao" é a abstracao do núcleo; por enquanto só
   deliberacoes (pautas com Sim/Nao/Abstencao) estao implementadas. A eleicao de chapas
   deve entrar aqui como um novo tipo, reaproveitando a mesma entidade e o mesmo fluxo. */
public enum TipoVotacao {
    DELIBERACAO
}
