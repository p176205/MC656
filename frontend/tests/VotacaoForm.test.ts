import { describe, expect, it } from "vitest";
import {
  criarCadastroVotacao,
  criarChapaVazia,
  estrategiasFormularioPorTipo,
  obterEstrategiaFormulario,
  temErrosValidacao,
  validarVotacaoForm,
  type VotacaoFormValues,
} from "../components/votacoes/VotacaoForm";

const deliberacaoValida: VotacaoFormValues = {
  tipo: "DELIBERACAO",
  nome: "Reforma da portaria",
  descricao: "Aprovar o orçamento para reforma da portaria.",
  regraAprovacao: "MAIORIA_SIMPLES",
  percentualMinimo: "60.5",
  quorumMinimo: "25.25",
  dataInicio: "2026-11-01T10:00",
  dataFim: "2026-11-02T10:00",
  chapas: [],
};

const eleicaoValida: VotacaoFormValues = {
  tipo: "ELEICAO",
  nome: "Eleição da Diretoria 2026",
  descricao: "Escolha da nova gestão do centro acadêmico.",
  regraAprovacao: "MAIORIA_SIMPLES",
  percentualMinimo: "",
  quorumMinimo: "30",
  dataInicio: "2026-11-01T10:00",
  dataFim: "2026-11-02T10:00",
  chapas: [
    {
      nome: "Chapa Integração",
      proposta: "Mais eventos e transparência",
      membrosIds: [11, 12],
    },
    {
      nome: "Chapa Renovação",
      proposta: "Melhoria dos espaços de estudo",
      membrosIds: [13],
    },
  ],
};

describe("formulário de votação (Strategy DELIBERACAO e ELEICAO)", () => {
  it("define rótulos específicos para DELIBERACAO e ELEICAO via Strategy", () => {
    const estrategiaDeliberacao = obterEstrategiaFormulario("DELIBERACAO");
    expect(estrategiaDeliberacao.rotuloTitulo).toBe("Título da Pauta");
    expect(estrategiaDeliberacao.rotuloDescricao).toBe("Descrição da Pauta");

    const estrategiaEleicao = obterEstrategiaFormulario("ELEICAO");
    expect(estrategiaEleicao.rotuloTitulo).toBe("Título da Eleição");
    expect(estrategiaEleicao.rotuloDescricao).toBe("Descrição da Eleição");

    expect(estrategiasFormularioPorTipo.DELIBERACAO).toBe(estrategiaDeliberacao);
    expect(estrategiasFormularioPorTipo.ELEICAO).toBe(estrategiaEleicao);
  });

  it("valida deliberação sem exigir chapas e valida campos comuns", () => {
    expect(validarVotacaoForm(deliberacaoValida)).toEqual({});
    expect(temErrosValidacao(validarVotacaoForm(deliberacaoValida))).toBe(false);

    const erros = validarVotacaoForm({
      ...deliberacaoValida,
      nome: " ",
      descricao: "",
      regraAprovacao: "PERCENTUAL_MINIMO",
      percentualMinimo: "100.1",
      quorumMinimo: "-1",
      dataFim: deliberacaoValida.dataInicio,
    });

    expect(erros.nome).toBeTruthy();
    expect(erros.descricao).toBeTruthy();
    expect(erros.percentualMinimo).toBeTruthy();
    expect(erros.quorumMinimo).toBeTruthy();
    expect(erros.dataFim).toBeTruthy();
    expect(temErrosValidacao(erros)).toBe(true);
    expect(
      validarVotacaoForm({ ...deliberacaoValida, regraAprovacao: "" }).regraAprovacao,
    ).toBeTruthy();
  });

  it("valida eleição exigindo chapas, nomes únicos e membros sem repetição", () => {
    expect(validarVotacaoForm(eleicaoValida)).toEqual({});
    expect(temErrosValidacao(validarVotacaoForm(eleicaoValida))).toBe(false);

    // Sem chapas
    const semChapas = validarVotacaoForm({
      ...eleicaoValida,
      chapas: [],
    });
    expect(semChapas.chapas).toBeTruthy();
    expect(temErrosValidacao(semChapas)).toBe(true);

    // Chapa vazia (sem nome e sem membros)
    const comChapaVazia = validarVotacaoForm({
      ...eleicaoValida,
      chapas: [criarChapaVazia()],
    });
    expect(comChapaVazia.chapasItens?.[0]?.nome).toBeTruthy();
    expect(comChapaVazia.chapasItens?.[0]?.membrosIds).toBeTruthy();
    expect(temErrosValidacao(comChapaVazia)).toBe(true);

    // Nomes de chapa duplicados (case-insensitive) e membro repetido em duas chapas
    const duplicados = validarVotacaoForm({
      ...eleicaoValida,
      chapas: [
        { nome: "Chapa A", proposta: "Proposta 1", membrosIds: [11] },
        { nome: " chapa a ", proposta: "Proposta 2", membrosIds: [11] },
      ],
    });
    expect(duplicados.chapasItens?.[1]?.nome).toBeTruthy();
    expect(duplicados.chapasItens?.[1]?.membrosIds).toBeTruthy();
  });

  it("monta payloads de DELIBERACAO e ELEICAO sem os antigos atributos de contexto", () => {
    const payloadDeliberacao = criarCadastroVotacao(42, 7, deliberacaoValida);
    expect(payloadDeliberacao).toEqual({
      contextoId: 42,
      adminId: 7,
      tipo: "DELIBERACAO",
      titulo: "Reforma da portaria",
      descricao: "Aprovar o orçamento para reforma da portaria.",
      regraAprovacao: "MAIORIA_SIMPLES",
      percentualMinimo: null,
      quorumMinimo: 25.25,
      dataInicio: "2026-11-01T10:00:00",
      dataFim: "2026-11-02T10:00:00",
      chapas: null,
    });
    expect("pauta" in payloadDeliberacao).toBe(false);
    expect("participantesAptos" in payloadDeliberacao).toBe(false);
    expect("pesoVoto" in payloadDeliberacao).toBe(false);
    expect("unidadeApartamento" in payloadDeliberacao).toBe(false);
    expect("categoriaSegmento" in payloadDeliberacao).toBe(false);
    expect("elegibilidade" in payloadDeliberacao).toBe(false);

    const payloadEleicao = criarCadastroVotacao(42, 7, eleicaoValida);
    expect(payloadEleicao).toEqual({
      contextoId: 42,
      adminId: 7,
      tipo: "ELEICAO",
      titulo: "Eleição da Diretoria 2026",
      descricao: "Escolha da nova gestão do centro acadêmico.",
      regraAprovacao: "MAIORIA_SIMPLES",
      percentualMinimo: null,
      quorumMinimo: 30,
      dataInicio: "2026-11-01T10:00:00",
      dataFim: "2026-11-02T10:00:00",
      chapas: [
        {
          nome: "Chapa Integração",
          proposta: "Mais eventos e transparência",
          membrosIds: [11, 12],
        },
        {
          nome: "Chapa Renovação",
          proposta: "Melhoria dos espaços de estudo",
          membrosIds: [13],
        },
      ],
    });
  });
});
