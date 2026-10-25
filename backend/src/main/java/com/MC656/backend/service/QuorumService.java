package com.MC656.backend.service;

import com.MC656.backend.entity.TipoContexto;
import com.MC656.backend.entity.Voto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/* Escolhe a QuorumStrategy de acordo com o tipo do contexto. O Spring injeta todas as
   implementacoes; para suportar um novo contexto basta criar outra @Component. */
@Service
public class QuorumService {

    private final List<QuorumStrategy> estrategias;

    public QuorumService(List<QuorumStrategy> estrategias) {
        this.estrategias = estrategias;
    }

    public boolean quorumAtingido(TipoContexto tipoContexto, double quorumMinimo,
                                  List<Voto> votos, Map<Long, Double> eleitorado) {
        QuorumStrategy estrategia = estrategias.stream()
                .filter(e -> e.suporta(tipoContexto))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma regra de quórum definida para o contexto " + tipoContexto));
        return estrategia.quorumAtingido(quorumMinimo, votos, eleitorado);
    }
}
