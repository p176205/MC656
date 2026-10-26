package com.MC656.backend.service;

import com.MC656.backend.entity.TipoContexto;
import com.MC656.backend.entity.Voto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/* Condominio: o quorum eh medido pelo peso (ex.: fracao ideal das unidades).
   participacao = soma dos pesos dos votos / soma dos pesos do eleitorado */
@Component
public class QuorumPorPesoStrategy implements QuorumStrategy {

    @Override
    public boolean suporta(TipoContexto tipoContexto) {
        return tipoContexto == TipoContexto.CONDOMINIO;
    }

    @Override
    public boolean quorumAtingido(double quorumMinimo, List<Voto> votos, Map<Long, Double> eleitorado) {
        if (quorumMinimo == 0) {
            return true;
        }
        double pesoTotal = eleitorado.values().stream().mapToDouble(Double::doubleValue).sum();
        if (pesoTotal == 0) {
            return false;
        }
        double pesoVotante = votos.stream().mapToDouble(Voto::getPeso).sum();
        return 100.0 * pesoVotante / pesoTotal >= quorumMinimo;
    }
}
