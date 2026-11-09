package com.MC656.backend.service;

import com.MC656.backend.dto.ApuracaoResponseDTO;
import com.MC656.backend.dto.ResultadoChapaDTO;
import com.MC656.backend.entity.*;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* Estrategia de apuracao para votacoes do tipo ELEICAO (disputa entre chapas).
   Contabiliza os pesos de cada chapa, separa os votos em branco/abstencao e avalia
   vencedor unico, empate no topo ou exigencia de percentual minimo. */
@Component
public class ApuracaoEleicaoStrategy implements ApuracaoStrategy {

    @Override
    public boolean suporta(TipoVotacao tipoVotacao) {
        // Verifica se a estrategia eh valida de acordo com o tipo da votacao (deve ser ELEICAO)
        return tipoVotacao == TipoVotacao.ELEICAO;
    }

    @Override
    public ApuracaoResponseDTO apurar(Votacao votacao, List<Voto> votos, List<Chapa> chapas, boolean quorumAtingido) {
        // Metodo geral responsavel pela apuracao da ELEICAO
        double abstencoes = somarAbstencoes(votos);
        Map<Long, Double> pesosPorChapa = calcularPesosPorChapa(chapas, votos);
        double totalVotosValidos = pesosPorChapa.values().stream().mapToDouble(Double::doubleValue).sum();

        List<ResultadoChapaDTO> resultadosChapas = montarResultadosPorChapa(chapas, pesosPorChapa, totalVotosValidos);

        if (!quorumAtingido) {
            return construirResposta(votacao, ResultadoVotacao.SEM_QUORUM, false,
                    abstencoes, votos.size(), null, resultadosChapas, null);
        }

        if (totalVotosValidos <= 0 || resultadosChapas.isEmpty()) {
            return construirResposta(votacao, ResultadoVotacao.REJEITADA, true,
                    abstencoes, votos.size(), null, resultadosChapas, null);
        }

        List<ResultadoChapaDTO> chapasMaisVotadas = filtrarChapasMaisVotadas(resultadosChapas);
        Double percentualMaisVotada = chapasMaisVotadas.get(0).percentualVotosValidos();

        // Se mais de uma chapa empatou na primeira colocação, declara EMPATE
        if (chapasMaisVotadas.size() > 1) {
            return construirResposta(votacao, ResultadoVotacao.EMPATE, true,
                    abstencoes, votos.size(), percentualMaisVotada, resultadosChapas, null);
        }

        ResultadoChapaDTO candidataVencedora = chapasMaisVotadas.get(0);
        if (atingiuCriterioDeEleicao(votacao, candidataVencedora)) {
            return construirResposta(votacao, ResultadoVotacao.ELEITA, true,
                    abstencoes, votos.size(), percentualMaisVotada, resultadosChapas, candidataVencedora);
        }

        return construirResposta(votacao, ResultadoVotacao.REJEITADA, true,
                abstencoes, votos.size(), percentualMaisVotada, resultadosChapas, null);
    }

    private double somarAbstencoes(List<Voto> votos) {
        return votos.stream()
                .filter(v -> v.getOpcao() == OpcaoVoto.ABSTENCAO)
                .mapToDouble(Voto::getPeso)
                .sum();
    }

    private Map<Long, Double> calcularPesosPorChapa(List<Chapa> chapas, List<Voto> votos) {
        // Calcula os votos por chapa (considerando os pesos de cada voto)
        Map<Long, Double> pesosPorChapa = new LinkedHashMap<>();
        for (Chapa chapa : chapas) {
            double somaChapa = votos.stream()
                    .filter(v -> v.getChapa() != null && chapa.getId().equals(v.getChapa().getId()))
                    .mapToDouble(Voto::getPeso)
                    .sum();
            pesosPorChapa.put(chapa.getId(), somaChapa);
        }
        return pesosPorChapa;
    }

    private List<ResultadoChapaDTO> montarResultadosPorChapa(List<Chapa> chapas, Map<Long, Double> pesosPorChapa, double totalVotosValidos) {
        // Retorna os resultados para cada chapa (quantidade e percentual de votos, id e nome da chapa)
        return chapas.stream()
                .map(c -> {
                    double votosChapa = pesosPorChapa.getOrDefault(c.getId(), 0.0);
                    Double percentual = totalVotosValidos > 0 ? 100.0 * votosChapa / totalVotosValidos : null;
                    return new ResultadoChapaDTO(c.getId(), c.getNome(), votosChapa, percentual);
                })
                .toList();
    }

    private List<ResultadoChapaDTO> filtrarChapasMaisVotadas(List<ResultadoChapaDTO> resultadosChapas) {
        // Retorna resultados para chapas com maior quantidade de votos (mais de um se empatar)
        double maxVotos = resultadosChapas.stream()
                .mapToDouble(ResultadoChapaDTO::votos)
                .max()
                .orElse(0.0);

        return resultadosChapas.stream()
                .filter(r -> Math.abs(r.votos() - maxVotos) < 1e-9)   //maxVotos == votos da chapa
                .toList();
    }

    private boolean atingiuCriterioDeEleicao(Votacao votacao, ResultadoChapaDTO maisVotada) {
        // Retorna se a chapa com maior quantidade de votos foi eleita ou nao
        return switch (votacao.getRegraAprovacao()) {
            case MAIORIA_SIMPLES -> true;
            case PERCENTUAL_MINIMO -> maisVotada.percentualVotosValidos() != null
                    && maisVotada.percentualVotosValidos() >= votacao.getPercentualMinimo();
        };
    }

    private ApuracaoResponseDTO construirResposta(Votacao votacao, ResultadoVotacao resultado,
                                                  boolean quorumAtingido, double abstencoes,
                                                  int totalVotantes, Double percentualMaisVotada,
                                                  List<ResultadoChapaDTO> resultadosChapas,
                                                  ResultadoChapaDTO chapaVencedora) {
        // Retorna dto formatado com os dados necessarios para exposicao dos resultados 
        return new ApuracaoResponseDTO(
                votacao.getId(),
                votacao.getTitulo(),
                resultado,
                quorumAtingido,
                0.0,
                0.0,
                abstencoes,
                totalVotantes,
                votacao.getRegraAprovacao(),
                votacao.getPercentualMinimo(),
                percentualMaisVotada,
                resultadosChapas,
                chapaVencedora
        );
    }
}

