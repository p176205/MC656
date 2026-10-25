package com.MC656.backend.entity;

/* Criterio para aprovar uma pauta. Em ambos, abstencoes contam para o quorum,
   mas nao entram na conta de aprovacao (base = votos SIM + NAO). */
public enum RegraAprovacao {
    MAIORIA_SIMPLES,   // aprovada se peso(SIM) > peso(NAO)
    PERCENTUAL_MINIMO  // aprovada se peso(SIM) / peso(SIM + NAO) >= percentualMinimo
}
