package com.MC656.backend.service;

import com.MC656.backend.entity.TipoContexto;
import com.MC656.backend.entity.Voto;

import java.util.List;
import java.util.Map;

/* Padrao Strategy: cada contexto mede a participacao para quorum de um jeito.
   Abstencoes contam como participacao. */
public interface QuorumStrategy {

    // Indica para quais tipos de contexto esta estrategia vale
    boolean suporta(TipoContexto tipoContexto);

    /**
     * @param quorumMinimo percentual minimo de participacao (0 a 100)
     * @param votos        votos registrados na urna
     * @param eleitorado   id do usuario -> peso, para todos os aptos a votar no contexto
     */
    boolean quorumAtingido(double quorumMinimo, List<Voto> votos, Map<Long, Double> eleitorado);
}
