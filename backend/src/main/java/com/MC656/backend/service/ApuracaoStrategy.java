package com.MC656.backend.service;

import com.MC656.backend.dto.ApuracaoResponseDTO;
import com.MC656.backend.entity.Chapa;
import com.MC656.backend.entity.TipoVotacao;
import com.MC656.backend.entity.Votacao;
import com.MC656.backend.entity.Voto;

import java.util.List;

/* Padrao Strategy para apuracao de resultados.
   Cada modalidade de votacao (DELIBERACAO, ELEICAO) possui regras proprias
   para contabilizar os votos validos, verificar criterios de aprovacao e montar o resultado. */
public interface ApuracaoStrategy {

    // Indica qual tipo de votacao esta estrategia sabe apurar
    boolean suporta(TipoVotacao tipoVotacao);

    /**
     * Calcula o resultado final de uma votacao já encerrada.
     *
     * @param votacao        entidade da votacao encerrada
     * @param votos          lista de votos registrados na urna anônima
     * @param chapas         chapas concorrentes (vazio quando não aplicável)
     * @param quorumAtingido indica se o quórum mínimo do contexto foi alcançado
     * @return DTO com os totais apurados e o veredito final
     */
    ApuracaoResponseDTO apurar(Votacao votacao, List<Voto> votos, List<Chapa> chapas, boolean quorumAtingido);
}

