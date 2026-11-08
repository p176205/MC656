"use client";

import { useState, type FormEvent } from "react";
import type { VotacaoCadastro, VotacaoResponse } from "../../entities/entities";
import { cadastrarVotacao } from "../../services/apiFacade";

export type VotacaoFormValues = {
  nome: string;
  descricao: string;
  regraAprovacao: string;
  percentualMinimo: string;
  quorumMinimo: string;
  dataInicio: string;
  dataFim: string;
};

export type VotacaoFormErrors = Partial<Record<keyof VotacaoFormValues, string>>;

export function validarVotacaoForm(valores: VotacaoFormValues): VotacaoFormErrors {
  const erros: VotacaoFormErrors = {};
  if (!valores.nome.trim()) erros.nome = "Informe o nome.";
  if (!valores.descricao.trim()) erros.descricao = "Informe a descrição.";
  if (!valores.regraAprovacao) erros.regraAprovacao = "Selecione a regra de aprovação.";

  const percentual = Number(valores.percentualMinimo);
  if (!valores.percentualMinimo.trim() || !Number.isFinite(percentual) || percentual < 0 || percentual > 100) {
    erros.percentualMinimo = "Informe um percentual entre 0 e 100.";
  }

  const quorum = Number(valores.quorumMinimo);
  if (!valores.quorumMinimo.trim() || !Number.isFinite(quorum) || quorum < 0 || quorum > 100) {
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

  return erros;
}

export function criarCadastroVotacao(contextoId: number, valores: VotacaoFormValues): VotacaoCadastro {
  return {
    contextoId,
    nome: valores.nome.trim(),
    descricao: valores.descricao.trim(),
    regraAprovacao: valores.regraAprovacao as VotacaoCadastro["regraAprovacao"],
    percentualMinimo: Number(valores.percentualMinimo),
    quorumMinimo: Number(valores.quorumMinimo),
    dataInicio: valores.dataInicio.length === 16 ? `${valores.dataInicio}:00` : valores.dataInicio,
    dataFim: valores.dataFim.length === 16 ? `${valores.dataFim}:00` : valores.dataFim,
    status: "AGENDADA",
  };
}

const valoresIniciais: VotacaoFormValues = {
  nome: "",
  descricao: "",
  regraAprovacao: "",
  percentualMinimo: "",
  quorumMinimo: "",
  dataInicio: "",
  dataFim: "",
};

type Props = {
  contextoId: number;
};

export default function VotacaoForm({ contextoId }: Props) {
  const [valores, setValores] = useState<VotacaoFormValues>(valoresIniciais);
  const [erros, setErros] = useState<VotacaoFormErrors>({});
  const [erroApi, setErroApi] = useState<string | null>(null);
  const [votacaoCriada, setVotacaoCriada] = useState<VotacaoResponse | null>(null);
  const [enviando, setEnviando] = useState(false);

  function atualizarCampo(campo: keyof VotacaoFormValues, valor: string) {
    setValores((atuais) => ({ ...atuais, [campo]: valor }));
    setErros((atuais) => ({ ...atuais, [campo]: undefined }));
    setErroApi(null);
    setVotacaoCriada(null);
  }

  async function enviarFormulario(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    const errosValidacao = validarVotacaoForm(valores);
    setErros(errosValidacao);
    setErroApi(null);
    setVotacaoCriada(null);
    if (Object.keys(errosValidacao).length > 0) return;

    setEnviando(true);
    try {
      const resposta = await cadastrarVotacao(criarCadastroVotacao(contextoId, valores));
      setVotacaoCriada(resposta);
    } catch (erro) {
      setErroApi(erro instanceof Error ? erro.message : "Não foi possível cadastrar a votação.");
    } finally {
      setEnviando(false);
    }
  }

  function campoTexto(campo: "nome" | "descricao", rotulo: string, multiline = false) {
    const controle = multiline ? (
      <textarea
        id={campo}
        name={campo}
        required
        rows={4}
        value={valores[campo]}
        onChange={(evento) => atualizarCampo(campo, evento.target.value)}
        aria-invalid={Boolean(erros[campo])}
        aria-describedby={erros[campo] ? `${campo}-erro` : undefined}
        className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
      />
    ) : (
      <input
        id={campo}
        name={campo}
        type="text"
        required
        value={valores[campo]}
        onChange={(evento) => atualizarCampo(campo, evento.target.value)}
        aria-invalid={Boolean(erros[campo])}
        aria-describedby={erros[campo] ? `${campo}-erro` : undefined}
        className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
      />
    );

    return (
      <label htmlFor={campo} className="flex flex-col gap-1 font-medium text-gray-800">
        {rotulo}
        {controle}
        {erros[campo] && <span id={`${campo}-erro`} className="text-sm font-normal text-red-700">{erros[campo]}</span>}
      </label>
    );
  }

  function campoNumero(campo: "percentualMinimo" | "quorumMinimo", rotulo: string) {
    return (
      <label htmlFor={campo} className="flex flex-col gap-1 font-medium text-gray-800">
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
          className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
        />
        {erros[campo] && <span id={`${campo}-erro`} className="text-sm font-normal text-red-700">{erros[campo]}</span>}
      </label>
    );
  }

  function campoData(campo: "dataInicio" | "dataFim", rotulo: string) {
    return (
      <label htmlFor={campo} className="flex flex-col gap-1 font-medium text-gray-800">
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
          className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
        />
        {erros[campo] && <span id={`${campo}-erro`} className="text-sm font-normal text-red-700">{erros[campo]}</span>}
      </label>
    );
  }

  return (
    <form onSubmit={enviarFormulario} noValidate className="flex w-full max-w-2xl flex-col gap-5 rounded-lg border p-6 shadow-md">
      {campoTexto("nome", "Nome")}
      {campoTexto("descricao", "Descrição", true)}

      <label htmlFor="regraAprovacao" className="flex flex-col gap-1 font-medium text-gray-800">
        Regra de aprovação
        <select
          id="regraAprovacao"
          name="regraAprovacao"
          required
          value={valores.regraAprovacao}
          onChange={(evento) => atualizarCampo("regraAprovacao", evento.target.value)}
          aria-invalid={Boolean(erros.regraAprovacao)}
          aria-describedby={erros.regraAprovacao ? "regraAprovacao-erro" : undefined}
          className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
        >
          <option value="">Selecione uma regra</option>
          <option value="MAIORIA_SIMPLES">Maioria simples</option>
          <option value="MAIORIA_ABSOLUTA">Maioria absoluta</option>
          <option value="MAIORIA_QUALIFICADA">Maioria qualificada</option>
          <option value="UNANIMIDADE">Unanimidade</option>
        </select>
        {erros.regraAprovacao && <span id="regraAprovacao-erro" className="text-sm font-normal text-red-700">{erros.regraAprovacao}</span>}
      </label>

      {campoNumero("percentualMinimo", "Percentual mínimo")}
      {campoNumero("quorumMinimo", "Quórum mínimo")}
      {campoData("dataInicio", "Data e hora de início")}
      {campoData("dataFim", "Data e hora de fim")}

      {erroApi && <p role="alert" className="rounded-md bg-red-50 p-3 text-red-800">{erroApi}</p>}
      {votacaoCriada && (
        <p role="status" className="rounded-md bg-green-50 p-3 text-green-800">
          Votação “{votacaoCriada.nome}” cadastrada com sucesso (ID {votacaoCriada.id}).
        </p>
      )}

      <button
        type="submit"
        disabled={enviando}
        className="rounded-md bg-purple-700 p-3 font-medium text-white hover:bg-purple-800 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {enviando ? "Cadastrando..." : "Cadastrar votação"}
      </button>
    </form>
  );
}
