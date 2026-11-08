import { describe, expect, it } from "vitest";
import {
  criarCadastroVotacao,
  validarVotacaoForm,
  type VotacaoFormValues,
} from "../components/votacoes/VotacaoForm";

const valoresValidos: VotacaoFormValues = {
  nome: "Eleição de representante",
  descricao: "Escolha do representante.",
  regraAprovacao: "MAIORIA_SIMPLES",
  percentualMinimo: "60.5",
  quorumMinimo: "25.25",
  dataInicio: "2026-11-01T10:00",
  dataFim: "2026-11-02T10:00",
};

describe("formulário de votação", () => {
  it("valida valores obrigatórios, faixas numéricas e ordem das datas", () => {
    expect(validarVotacaoForm(valoresValidos)).toEqual({});

    const erros = validarVotacaoForm({
      ...valoresValidos,
      nome: " ",
      regraAprovacao: "",
      percentualMinimo: "100.1",
      quorumMinimo: "-1",
      dataFim: valoresValidos.dataInicio,
    });

    expect(erros.nome).toBeTruthy();
    expect(erros.regraAprovacao).toBeTruthy();
    expect(erros.percentualMinimo).toBeTruthy();
    expect(erros.quorumMinimo).toBeTruthy();
    expect(erros.dataFim).toBeTruthy();
  });

  it("converte números e datas para o contrato da API e define status inicial agendado", () => {
    expect(criarCadastroVotacao(42, valoresValidos)).toEqual({
      contextoId: 42,
      nome: "Eleição de representante",
      descricao: "Escolha do representante.",
      regraAprovacao: "MAIORIA_SIMPLES",
      percentualMinimo: 60.5,
      quorumMinimo: 25.25,
      dataInicio: "2026-11-01T10:00:00",
      dataFim: "2026-11-02T10:00:00",
      status: "AGENDADA",
    });
  });
});
