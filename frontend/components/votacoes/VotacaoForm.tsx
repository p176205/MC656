"use client";

import { useEffect, useState, type FormEvent } from "react";
import type {
  ChapaCadastro,
  MembroChapa,
  RegraAprovacao,
  TipoContexto,
  TipoVotacao,
  VotacaoCadastro,
  VotacaoResponse,
} from "../../entities/entities";
import {
  buscarMembrosDoContexto,
  cadastrarVotacao,
  getIdUser,
} from "../../services/apiFacade";
import { toast } from "sonner";

export type ChapaFormValue = {
  nome: string;
  proposta: string;
  membrosIds: number[];
};

export type ChapaFormErrors = {
  nome?: string;
  proposta?: string;
  membrosIds?: string;
};

export type VotacaoFormValues = {
  tipo: TipoVotacao;
  nome: string;
  descricao: string;
  regraAprovacao: string;
  percentualMinimo: string;
  quorumMinimo: string;
  dataInicio: string;
  dataFim: string;
  chapas: ChapaFormValue[];
};

export type VotacaoFormErrors = {
  tipo?: string;
  nome?: string;
  descricao?: string;
  regraAprovacao?: string;
  percentualMinimo?: string;
  quorumMinimo?: string;
  dataInicio?: string;
  dataFim?: string;
  chapas?: string;
  chapasItens?: Record<number, ChapaFormErrors>;
};

/* Padrão Strategy: encapsula os rótulos de interface, regras de validação específicas
   e construção do payload de acordo com o TipoVotacao (DELIBERACAO ou ELEICAO). */
export interface VotacaoFormStrategy {
  readonly tipo: TipoVotacao;
  readonly rotuloTitulo: string;
  readonly rotuloDescricao: string;
  readonly placeholderTitulo: string;
  readonly placeholderDescricao: string;
  validarEspecifico(valores: VotacaoFormValues, erros: VotacaoFormErrors): void;
  construirChapasPayload(valores: VotacaoFormValues): ChapaCadastro[] | null;
}

const deliberacaoFormStrategy: VotacaoFormStrategy = {
  tipo: "DELIBERACAO",
  rotuloTitulo: "Título da Pauta",
  rotuloDescricao: "Descrição da Pauta",
  placeholderTitulo: "Informe o título da pauta",
  placeholderDescricao: "Descreva os detalhes da pauta em deliberação",
  validarEspecifico() {
    // Deliberações não possuem chapas
  },
  construirChapasPayload() {
    return null;
  },
};

const eleicaoFormStrategy: VotacaoFormStrategy = {
  tipo: "ELEICAO",
  rotuloTitulo: "Título da Eleição",
  rotuloDescricao: "Descrição da Eleição",
  placeholderTitulo: "Informe o título da eleição",
  placeholderDescricao: "Descreva os detalhes da eleição",
  validarEspecifico(valores, erros) {
    if (!valores.chapas || valores.chapas.length === 0) {
      erros.chapas = "Adicione ao menos uma chapa para a eleição.";
      return;
    }

    const nomesNormalizados = new Set<string>();
    const membrosNaEleicao = new Set<number>();
    const chapasItens: Record<number, ChapaFormErrors> = {};

    valores.chapas.forEach((chapa, index) => {
      const errosChapa: ChapaFormErrors = {};
      const nomeLimpo = chapa.nome.trim();

      if (!nomeLimpo) {
        errosChapa.nome = "Informe o nome da chapa.";
      } else {
        const chaveNome = nomeLimpo.toLowerCase();
        if (nomesNormalizados.has(chaveNome)) {
          errosChapa.nome = "Não são permitidas chapas com nomes duplicados.";
        } else {
          nomesNormalizados.add(chaveNome);
        }
      }

      if (!chapa.membrosIds || chapa.membrosIds.length === 0) {
        errosChapa.membrosIds = "Selecione ao menos um membro do contexto para a chapa.";
      } else {
        for (const membroId of chapa.membrosIds) {
          if (membrosNaEleicao.has(membroId)) {
            errosChapa.membrosIds =
              "Um membro não pode participar de mais de uma chapa na mesma eleição.";
          } else {
            membrosNaEleicao.add(membroId);
          }
        }
      }

      if (Object.keys(errosChapa).length > 0) {
        chapasItens[index] = errosChapa;
      }
    });

    if (Object.keys(chapasItens).length > 0) {
      erros.chapasItens = chapasItens;
    }
  },
  construirChapasPayload(valores) {
    return valores.chapas.map((chapa) => ({
      nome: chapa.nome.trim(),
      proposta: chapa.proposta.trim() ? chapa.proposta.trim() : null,
      membrosIds: [...chapa.membrosIds],
    }));
  },
};

export const estrategiasFormularioPorTipo: Record<TipoVotacao, VotacaoFormStrategy> = {
  DELIBERACAO: deliberacaoFormStrategy,
  ELEICAO: eleicaoFormStrategy,
};

export function obterEstrategiaFormulario(tipo: TipoVotacao): VotacaoFormStrategy {
  return estrategiasFormularioPorTipo[tipo] ?? deliberacaoFormStrategy;
}

export function criarChapaVazia(): ChapaFormValue {
  return {
    nome: "",
    proposta: "",
    membrosIds: [],
  };
}

const regrasAprovacao: RegraAprovacao[] = [
  "MAIORIA_SIMPLES",
  "PERCENTUAL_MINIMO",
];

const tiposVotacao: TipoVotacao[] = ["DELIBERACAO", "ELEICAO"];

export function temErrosValidacao(erros: VotacaoFormErrors): boolean {
  const { chapasItens, ...errosGerais } = erros;
  const possuiErroGeral = Object.values(errosGerais).some((msg) => Boolean(msg));
  const possuiErroChapa = chapasItens
    ? Object.values(chapasItens).some((item) => Object.values(item).some((msg) => Boolean(msg)))
    : false;
  return possuiErroGeral || possuiErroChapa;
}

export function validarVotacaoForm(valores: VotacaoFormValues): VotacaoFormErrors {
  const erros: VotacaoFormErrors = {};

  if (!tiposVotacao.includes(valores.tipo)) {
    erros.tipo = "Selecione o tipo da votação.";
  }
  if (!valores.nome.trim()) erros.nome = "Informe o título.";
  if (!valores.descricao.trim()) erros.descricao = "Informe a descrição.";
  if (!regrasAprovacao.includes(valores.regraAprovacao as RegraAprovacao)) {
    erros.regraAprovacao = "Selecione a regra de aprovação.";
  }

  if (valores.regraAprovacao === "PERCENTUAL_MINIMO") {
    const percentual = Number(valores.percentualMinimo);
    if (
      !valores.percentualMinimo.trim() ||
      !Number.isFinite(percentual) ||
      percentual <= 0 ||
      percentual > 100
    ) {
      erros.percentualMinimo = "Informe um percentual maior que 0 e até 100.";
    }
  }

  const quorum = Number(valores.quorumMinimo);
  if (
    !valores.quorumMinimo.trim() ||
    !Number.isFinite(quorum) ||
    quorum < 0 ||
    quorum > 100
  ) {
    erros.quorumMinimo = "Informe um quórum entre 0 e 100.";
  }

  if (!valores.dataInicio) erros.dataInicio = "Informe a data de início.";
  if (!valores.dataFim) erros.dataFim = "Informe a data de fim.";
  if (
    valores.dataInicio &&
    valores.dataFim &&
    (!Number.isFinite(Date.parse(valores.dataInicio)) ||
      !Number.isFinite(Date.parse(valores.dataFim)) ||
      Date.parse(valores.dataFim) <= Date.parse(valores.dataInicio))
  ) {
    erros.dataFim = "A data de fim deve ser posterior à data de início.";
  }

  const estrategia = obterEstrategiaFormulario(valores.tipo);
  estrategia.validarEspecifico(valores, erros);

  return erros;
}

function dataIsoLocal(valor: string): string {
  return valor.length === 16 ? `${valor}:00` : valor;
}

export function criarCadastroVotacao(
  contextoId: number,
  adminId: number,
  valores: VotacaoFormValues,
): VotacaoCadastro {
  if (!regrasAprovacao.includes(valores.regraAprovacao as RegraAprovacao)) {
    throw new Error("Selecione uma regra de aprovação válida.");
  }

  const estrategia = obterEstrategiaFormulario(valores.tipo);

  return {
    contextoId,
    adminId,
    tipo: estrategia.tipo,
    titulo: valores.nome.trim(),
    descricao: valores.descricao.trim(),
    regraAprovacao: valores.regraAprovacao as RegraAprovacao,
    percentualMinimo:
      valores.regraAprovacao === "PERCENTUAL_MINIMO"
        ? Number(valores.percentualMinimo)
        : null,
    quorumMinimo: Number(valores.quorumMinimo),
    dataInicio: dataIsoLocal(valores.dataInicio),
    dataFim: dataIsoLocal(valores.dataFim),
    chapas: estrategia.construirChapasPayload(valores),
  };
}

const valoresIniciais: VotacaoFormValues = {
  tipo: "DELIBERACAO",
  nome: "",
  descricao: "",
  regraAprovacao: "",
  percentualMinimo: "",
  quorumMinimo: "",
  dataInicio: "",
  dataFim: "",
  chapas: [],
};

type Props = {
  contextoId: number;
  tipoContexto?: TipoContexto;
  adminId?: number;
  membrosContexto?: MembroChapa[];
};

export default function VotacaoForm({
  contextoId,
  adminId: adminIdProp,
  membrosContexto: membrosContextoProp,
}: Props) {
  const [valores, setValores] = useState<VotacaoFormValues>(valoresIniciais);
  const [erros, setErros] = useState<VotacaoFormErrors>({});
  const [erroApi, setErroApi] = useState<string | null>(null);
  const [votacaoCriada, setVotacaoCriada] = useState<VotacaoResponse | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [membrosCarregados, setMembrosCarregados] = useState<MembroChapa[]>([]);
  const [adminIdCarregado, setAdminIdCarregado] = useState<number | undefined>(
    undefined,
  );

  const membrosContexto = membrosContextoProp ?? membrosCarregados;
  const adminId = adminIdProp ?? adminIdCarregado;

  useEffect(() => {
    if (membrosContextoProp !== undefined) {
      return;
    }
    let cancelado = false;
    async function carregarMembros() {
      try {
        const lista = await buscarMembrosDoContexto(contextoId);
        if (!cancelado) setMembrosCarregados(lista);
      } catch {
        // Caso o endpoint falhe, mantém lista atual
      }
    }
    void carregarMembros();
    return () => {
      cancelado = true;
    };
  }, [contextoId, membrosContextoProp]);

  useEffect(() => {
    if (adminIdProp !== undefined) {
      return;
    }
    let cancelado = false;
    async function carregarAdmin() {
      try {
        const usuario = await getIdUser();
        if (!cancelado) setAdminIdCarregado(usuario.id);
      } catch {
        // Admin será validado no envio
      }
    }
    void carregarAdmin();
    return () => {
      cancelado = true;
    };
  }, [adminIdProp]);

  const estrategiaAtual = obterEstrategiaFormulario(valores.tipo);

  function selecionarTipoVotacao(novoTipo: TipoVotacao) {
    setValores((atuais) => ({
      ...atuais,
      tipo: novoTipo,
      chapas: novoTipo === "DELIBERACAO" ? [] : atuais.chapas,
    }));
    setErros((atuais) => ({
      ...atuais,
      tipo: undefined,
      chapas: undefined,
      chapasItens: undefined,
    }));
    setErroApi(null);
    setVotacaoCriada(null);
  }

  function atualizarCampo(
    campo: Exclude<keyof VotacaoFormValues, "chapas" | "tipo">,
    valor: string,
  ) {
    setValores((atuais) => ({ ...atuais, [campo]: valor }));
    setErros((atuais) => ({ ...atuais, [campo]: undefined }));
    setErroApi(null);
    setVotacaoCriada(null);
  }

  function adicionarChapa() {
    setValores((atuais) => ({
      ...atuais,
      chapas: [...atuais.chapas, criarChapaVazia()],
    }));
    setErros((atuais) => ({ ...atuais, chapas: undefined }));
    setErroApi(null);
    setVotacaoCriada(null);
  }

  function removerChapa(indice: number) {
    setValores((atuais) => ({
      ...atuais,
      chapas: atuais.chapas.filter((_, idx) => idx !== indice),
    }));
    setErros((atuais) => ({
      ...atuais,
      chapas: undefined,
      chapasItens: undefined,
    }));
  }

  function atualizarCampoChapa(
    indice: number,
    campo: "nome" | "proposta",
    valor: string,
  ) {
    setValores((atuais) => ({
      ...atuais,
      chapas: atuais.chapas.map((chapa, idx) =>
        idx === indice ? { ...chapa, [campo]: valor } : chapa,
      ),
    }));
    setErros((atuais) => {
      if (!atuais.chapasItens?.[indice]) return atuais;
      return {
        ...atuais,
        chapasItens: {
          ...atuais.chapasItens,
          [indice]: {
            ...atuais.chapasItens[indice],
            [campo]: undefined,
          },
        },
      };
    });
  }

  function adicionarMembroNaChapa(indiceChapa: number, membroIdValor: string) {
    const membroId = Number(membroIdValor);
    if (!Number.isSafeInteger(membroId) || membroId <= 0) return;

    setValores((atuais) => ({
      ...atuais,
      chapas: atuais.chapas.map((chapa, idx) => {
        if (idx !== indiceChapa || chapa.membrosIds.includes(membroId)) {
          return chapa;
        }
        return {
          ...chapa,
          membrosIds: [...chapa.membrosIds, membroId],
        };
      }),
    }));
    setErros((atuais) => {
      if (!atuais.chapasItens?.[indiceChapa]) return atuais;
      return {
        ...atuais,
        chapasItens: {
          ...atuais.chapasItens,
          [indiceChapa]: {
            ...atuais.chapasItens[indiceChapa],
            membrosIds: undefined,
          },
        },
      };
    });
  }

  function removerMembroDaChapa(indiceChapa: number, membroId: number) {
    setValores((atuais) => ({
      ...atuais,
      chapas: atuais.chapas.map((chapa, idx) =>
        idx === indiceChapa
          ? {
              ...chapa,
              membrosIds: chapa.membrosIds.filter((id) => id !== membroId),
            }
          : chapa,
      ),
    }));
  }

  async function enviarFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const errosValidacao = validarVotacaoForm(valores);
    setErros(errosValidacao);
    setErroApi(null);
    setVotacaoCriada(null);
    if (temErrosValidacao(errosValidacao)) return;

    setEnviando(true);
    try {
      let idAdminResolvido = adminId;
      if (idAdminResolvido === undefined) {
        const usuario = await getIdUser();
        idAdminResolvido = usuario.id;
        setAdminIdCarregado(usuario.id);
      }

      const payload = criarCadastroVotacao(contextoId, idAdminResolvido, valores);
      const resposta = await cadastrarVotacao(payload);
      setVotacaoCriada(resposta);
      toast.success("Votação cadastrada com sucesso!");
    } catch (erro) {
      setErroApi(
        erro instanceof Error
          ? erro.message
          : "Não foi possível cadastrar a votação.",
      );
      toast.error("Não foi possível cadastrar a votação!");
    } finally {
      setEnviando(false);
    }
  }

  function campoTexto(
    campo: "nome" | "descricao",
    rotulo: string,
    placeholder: string,
    multiline = false,
  ) {
    const controle = multiline ? (
      <textarea
        id={campo}
        name={campo}
        required
        rows={4}
        placeholder={placeholder}
        value={valores[campo]}
        onChange={(evento) => atualizarCampo(campo, evento.target.value)}
        aria-invalid={Boolean(erros[campo])}
        aria-describedby={erros[campo] ? `${campo}-erro` : undefined}
        className="rounded-md border border-indigo-500 p-2 outline-none focus:border-purple-600"
      />
    ) : (
      <input
        id={campo}
        name={campo}
        type="text"
        required
        placeholder={placeholder}
        value={valores[campo]}
        onChange={(evento) => atualizarCampo(campo, evento.target.value)}
        aria-invalid={Boolean(erros[campo])}
        aria-describedby={erros[campo] ? `${campo}-erro` : undefined}
        className="rounded-md border border-indigo-500 p-2 outline-none focus:border-purple-600"
      />
    );

    return (
      <label
        htmlFor={campo}
        className="flex flex-col gap-1 font-medium text-gray-800"
      >
        {rotulo}
        {controle}
        {erros[campo] && (
          <span
            id={`${campo}-erro`}
            className="text-sm font-normal text-red-700"
          >
            {erros[campo]}
          </span>
        )}
      </label>
    );
  }

  function campoNumero(
    campo: "percentualMinimo" | "quorumMinimo",
    rotulo: string,
  ) {
    return (
      <label
        htmlFor={campo}
        className="flex flex-col gap-1 font-medium text-gray-800"
      >
        {rotulo} (%)
        <input
          id={campo}
          name={campo}
          type="number"
          min="0"
          max="100"
          step="0.01"
          required
          value={valores[campo]}
          onChange={(evento) => atualizarCampo(campo, evento.target.value)}
          aria-invalid={Boolean(erros[campo])}
          aria-describedby={erros[campo] ? `${campo}-erro` : undefined}
          className="rounded-md border border-indigo-500 p-2 outline-none focus:border-purple-600"
        />
        {erros[campo] && (
          <span
            id={`${campo}-erro`}
            className="text-sm font-normal text-red-700"
          >
            {erros[campo]}
          </span>
        )}
      </label>
    );
  }

  function campoData(campo: "dataInicio" | "dataFim", rotulo: string) {
    return (
      <label
        htmlFor={campo}
        className="flex flex-col gap-1 font-medium text-gray-800"
      >
        {rotulo}
        <input
          id={campo}
          name={campo}
          type="datetime-local"
          required
          value={valores[campo]}
          onChange={(evento) => atualizarCampo(campo, evento.target.value)}
          aria-invalid={Boolean(erros[campo])}
          aria-describedby={erros[campo] ? `${campo}-erro` : undefined}
          className="rounded-md border border-indigo-500 p-2 outline-none focus:border-purple-600"
        />
        {erros[campo] && (
          <span
            id={`${campo}-erro`}
            className="text-sm font-normal text-red-700"
          >
            {erros[campo]}
          </span>
        )}
      </label>
    );
  }

  // Conjunto de membros já vinculados a alguma chapa para orientar a seleção
  const membrosJaAlocados = new Set<number>(
    valores.chapas.flatMap((chapa) => chapa.membrosIds),
  );

  return (
    <section className="flex items-center justify-center">
      <form
        onSubmit={enviarFormulario}
        noValidate
        className="flex w-full max-w-3xl flex-col gap-5 rounded-xl border border-indigo-500 border-r-8 border-r-indigo-500 px-8 py-10 shadow-md"
      >
        <div className="flex flex-col gap-2">
          <span className="font-medium text-gray-800">Tipo da votação</span>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              aria-pressed={valores.tipo === "DELIBERACAO"}
              onClick={() => selecionarTipoVotacao("DELIBERACAO")}
              className={`rounded-md border border-indigo-500 p-3 font-medium transition-colors ${
                valores.tipo === "DELIBERACAO"
                  ? "bg-indigo-500 text-white"
                  : "bg-white text-indigo-700 hover:bg-indigo-50"
              }`}
            >
              DELIBERACAO
            </button>
            <button
              type="button"
              aria-pressed={valores.tipo === "ELEICAO"}
              onClick={() => selecionarTipoVotacao("ELEICAO")}
              className={`rounded-md border border-indigo-500 p-3 font-medium transition-colors ${
                valores.tipo === "ELEICAO"
                  ? "bg-indigo-500 text-white"
                  : "bg-white text-indigo-700 hover:bg-indigo-50"
              }`}
            >
              ELEICAO
            </button>
          </div>
          {erros.tipo && (
            <span className="text-sm font-normal text-red-700">{erros.tipo}</span>
          )}
        </div>

        {campoTexto(
          "nome",
          estrategiaAtual.rotuloTitulo,
          estrategiaAtual.placeholderTitulo,
        )}
        {campoTexto(
          "descricao",
          estrategiaAtual.rotuloDescricao,
          estrategiaAtual.placeholderDescricao,
          true,
        )}

        <label
          htmlFor="regraAprovacao"
          className="flex flex-col gap-1 font-medium text-gray-800"
        >
          Regra de aprovação
          <select
            id="regraAprovacao"
            name="regraAprovacao"
            required
            value={valores.regraAprovacao}
            onChange={(evento) =>
              atualizarCampo("regraAprovacao", evento.target.value)
            }
            aria-invalid={Boolean(erros.regraAprovacao)}
            aria-describedby={
              erros.regraAprovacao ? "regraAprovacao-erro" : undefined
            }
            className="rounded-md border border-indigo-500 p-2 outline-none focus:border-indigo-600"
          >
            <option value="">Selecione uma regra</option>
            <option value="MAIORIA_SIMPLES">Maioria simples</option>
            <option value="PERCENTUAL_MINIMO">Percentual mínimo</option>
          </select>
          {erros.regraAprovacao && (
            <span
              id="regraAprovacao-erro"
              className="text-sm font-normal text-red-700"
            >
              {erros.regraAprovacao}
            </span>
          )}
        </label>

        {valores.regraAprovacao === "PERCENTUAL_MINIMO" &&
          campoNumero("percentualMinimo", "Percentual mínimo")}
        {campoNumero("quorumMinimo", "Quórum mínimo")}

        {campoData("dataInicio", "Data e hora de início")}
        {campoData("dataFim", "Data e hora de fim")}

        {valores.tipo === "ELEICAO" && (
          <div className="flex flex-col gap-4 border-t border-indigo-200 pt-4">
            <div className="flex items-center justify-between">
              <span className="text-lg font-semibold text-gray-800">
                Chapas da Eleição
              </span>
              <button
                type="button"
                onClick={adicionarChapa}
                className="rounded-md bg-indigo-100 px-4 py-2 font-medium text-indigo-800 hover:bg-indigo-200"
              >
                Adicionar Chapa
              </button>
            </div>

            {erros.chapas && (
              <span className="text-sm font-normal text-red-700">
                {erros.chapas}
              </span>
            )}

            {valores.chapas.map((chapa, index) => {
              const errosChapa = erros.chapasItens?.[index];
              return (
                <div
                  key={index}
                  className="flex flex-col gap-4 rounded-lg border border-indigo-300 bg-indigo-50/30 p-4"
                >
                  <div className="flex items-center justify-between">
                    <h3 className="font-semibold text-indigo-950">
                      Chapa {index + 1}
                    </h3>
                    <button
                      type="button"
                      onClick={() => removerChapa(index)}
                      className="text-sm font-medium text-red-600 hover:text-red-800"
                    >
                      Remover chapa
                    </button>
                  </div>

                  <label
                    htmlFor={`chapa-nome-${index}`}
                    className="flex flex-col gap-1 font-medium text-gray-800"
                  >
                    Nome da Chapa
                    <input
                      id={`chapa-nome-${index}`}
                      name={`chapa-nome-${index}`}
                      type="text"
                      required
                      value={chapa.nome}
                      onChange={(evento) =>
                        atualizarCampoChapa(index, "nome", evento.target.value)
                      }
                      aria-invalid={Boolean(errosChapa?.nome)}
                      className="rounded-md border border-indigo-500 bg-white p-2 outline-none focus:border-purple-600"
                    />
                    {errosChapa?.nome && (
                      <span className="text-sm font-normal text-red-700">
                        {errosChapa.nome}
                      </span>
                    )}
                  </label>

                  <label
                    htmlFor={`chapa-proposta-${index}`}
                    className="flex flex-col gap-1 font-medium text-gray-800"
                  >
                    Descrição/Proposta da Chapa
                    <textarea
                      id={`chapa-proposta-${index}`}
                      name={`chapa-proposta-${index}`}
                      rows={3}
                      value={chapa.proposta}
                      onChange={(evento) =>
                        atualizarCampoChapa(
                          index,
                          "proposta",
                          evento.target.value,
                        )
                      }
                      className="rounded-md border border-indigo-500 bg-white p-2 outline-none focus:border-purple-600"
                    />
                  </label>

                  <div className="flex flex-col gap-2">
                    <label
                      htmlFor={`chapa-membros-${index}`}
                      className="flex flex-col gap-1 font-medium text-gray-800"
                    >
                      Membros da Chapa
                      <select
                        id={`chapa-membros-${index}`}
                        name={`chapa-membros-${index}`}
                        value=""
                        onChange={(evento) =>
                          adicionarMembroNaChapa(index, evento.target.value)
                        }
                        aria-invalid={Boolean(errosChapa?.membrosIds)}
                        className="rounded-md border border-indigo-500 bg-white p-2 outline-none focus:border-indigo-600"
                      >
                        <option value="">
                          Selecione um membro presente no contexto
                        </option>
                        {membrosContexto.map((membro) => {
                          const jaSelecionado = membrosJaAlocados.has(membro.id);
                          return (
                            <option
                              key={membro.id}
                              value={membro.id}
                              disabled={jaSelecionado}
                            >
                              {membro.nome} ({membro.email})
                              {jaSelecionado ? " - já selecionado" : ""}
                            </option>
                          );
                        })}
                      </select>
                    </label>

                    {chapa.membrosIds.length > 0 && (
                      <div className="flex flex-wrap gap-2 pt-1">
                        {chapa.membrosIds.map((membroId) => {
                          const membro = membrosContexto.find(
                            (m) => m.id === membroId,
                          );
                          return (
                            <span
                              key={membroId}
                              className="inline-flex items-center gap-2 rounded-full border border-indigo-400 bg-indigo-100 px-3 py-1 text-sm text-indigo-900"
                            >
                              {membro
                                ? `${membro.nome} (${membro.email})`
                                : `Membro #${membroId}`}
                              <button
                                type="button"
                                onClick={() =>
                                  removerMembroDaChapa(index, membroId)
                                }
                                aria-label={`Remover membro ${membro?.nome ?? membroId}`}
                                className="font-bold text-indigo-700 hover:text-red-700"
                              >
                                ×
                              </button>
                            </span>
                          );
                        })}
                      </div>
                    )}

                    {errosChapa?.membrosIds && (
                      <span className="text-sm font-normal text-red-700">
                        {errosChapa.membrosIds}
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {erroApi && (
          <p
            role="alert"
            className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700"
          >
            {erroApi}
          </p>
        )}

        {votacaoCriada && (
          <p
            role="status"
            className="rounded-md border border-green-200 bg-green-50 p-3 text-sm text-green-800"
          >
            Votação &ldquo;{votacaoCriada.titulo}&rdquo; criada com sucesso!
          </p>
        )}

        <button
          type="submit"
          disabled={enviando}
          className="rounded-md bg-indigo-500 p-3 font-medium text-white hover:bg-indigo-600 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {enviando ? "Cadastrando..." : "Cadastrar votação"}
        </button>
      </form>
    </section>
  );
}
