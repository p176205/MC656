package com.MC656.backend.service;

import com.MC656.backend.entity.TipoContexto;
import com.MC656.backend.entity.Voto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/* Assembleia e Centro Academico: cada pessoa conta 1, independente do peso.
   participacao = num de votantes / num de aptos */
@Component
public class QuorumPorCabecaStrategy implements QuorumStrategy {

    @Override
    public boolean suporta(TipoContexto tipoContexto) {
        return tipoContexto == TipoContexto.ASSEMBLEIA || tipoContexto == TipoContexto.CENTRO_ACADEMICO;
    }

    @Override
    public boolean quorumAtingido(double quorumMinimo, List<Voto> votos, Map<Long, Double> eleitorado) {
        if (quorumMinimo == 0) {
            return true;
        }
        if (eleitorado.isEmpty()) {
            return false;
        }
        double participacao = 100.0 * votos.size() / eleitorado.size();
        return participacao >= quorumMinimo;
    }
}
