import { describe, expect, it } from "vitest";
import {
  camposEspecificosPorTipoContexto,
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
  pauta: "Escolha de representante",
  participantesAptos: "25",
  pesoVoto: "1.5",
  unidadeApartamento: "Apto 12",
  categoriaSegmento: "Graduação",
  elegibilidade: "Estudantes regularmente matriculados",
};

describe("formulário de votação", () => {
  it("valida somente os campos específicos do tipo do contexto", () => {
    expect(camposEspecificosPorTipoContexto.ASSEMBLEIA).toEqual(["pauta", "participantesAptos"]);
    expect(camposEspecificosPorTipoContexto.CONDOMINIO).toEqual(["pesoVoto", "unidadeApartamento"]);
    expect(camposEspecificosPorTipoContexto.CENTRO_ACADEMICO).toEqual(["categoriaSegmento", "elegibilidade"]);
    expect(validarVotacaoForm("ASSEMBLEIA", valoresValidos)).toEqual({});
    expect(validarVotacaoForm("CONDOMINIO", valoresValidos)).toEqual({});
    expect(validarVotacaoForm("CENTRO_ACADEMICO", valoresValidos)).toEqual({});

    const errosAssembleia = validarVotacaoForm("ASSEMBLEIA", {
      ...valoresValidos,
      pauta: "",
      participantesAptos: "1.5",
      pesoVoto: "",
      unidadeApartamento: "",
      categoriaSegmento: "",
      elegibilidade: "",
    });
    expect(errosAssembleia.pauta).toBeTruthy();
    expect(errosAssembleia.participantesAptos).toBeTruthy();
    expect(errosAssembleia.pesoVoto).toBeUndefined();
    expect(errosAssembleia.categoriaSegmento).toBeUndefined();

    const errosCondominio = validarVotacaoForm("CONDOMINIO", {
      ...valoresValidos,
      pauta: "",
      participantesAptos: "",
      pesoVoto: "0",
      unidadeApartamento: " ",
      categoriaSegmento: "",
      elegibilidade: "",
    });
    expect(errosCondominio.pesoVoto).toBeTruthy();
    expect(errosCondominio.unidadeApartamento).toBeTruthy();
    expect(errosCondominio.pauta).toBeUndefined();

    const errosCentro = validarVotacaoForm("CENTRO_ACADEMICO", {
      ...valoresValidos,
      pauta: "",
      participantesAptos: "",
      pesoVoto: "",
      unidadeApartamento: "",
      categoriaSegmento: "",
      elegibilidade: "",
    });
    expect(errosCentro.categoriaSegmento).toBeTruthy();
    expect(errosCentro.elegibilidade).toBeTruthy();
    expect(errosCentro.participantesAptos).toBeUndefined();
  });

  it("valida os campos comuns, limites e ordem das datas", () => {
    const erros = validarVotacaoForm("CONDOMINIO", {
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

  it("monta payloads específicos sem incluir campos de outros tipos", () => {
    const assembleia = criarCadastroVotacao(42, "ASSEMBLEIA", valoresValidos);
    expect(assembleia).toMatchObject({
      contextoId: 42,
      nome: "Eleição de representante",
      descricao: "Escolha do representante.",
      regraAprovacao: "MAIORIA_SIMPLES",
      percentualMinimo: 60.5,
      quorumMinimo: 25.25,
      dataInicio: "2026-11-01T10:00:00",
      dataFim: "2026-11-02T10:00:00",
      status: "AGENDADA",
      pauta: "Escolha de representante",
      participantesAptos: 25,
    });
    expect("pesoVoto" in assembleia).toBe(false);
    expect("categoriaSegmento" in assembleia).toBe(false);

    const condominio = criarCadastroVotacao(42, "CONDOMINIO", valoresValidos);
    expect(condominio).toMatchObject({ pesoVoto: 1.5, unidadeApartamento: "Apto 12" });
    expect("pauta" in condominio).toBe(false);
    expect("elegibilidade" in condominio).toBe(false);

    const centro = criarCadastroVotacao(42, "CENTRO_ACADEMICO", valoresValidos);
    expect(centro).toMatchObject({
      categoriaSegmento: "Graduação",
      elegibilidade: "Estudantes regularmente matriculados",
    });
    expect("participantesAptos" in centro).toBe(false);
    expect("unidadeApartamento" in centro).toBe(false);
  });
});
