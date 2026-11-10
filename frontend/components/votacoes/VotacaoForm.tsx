"use client";

import { useState, type FormEvent } from "react";
import type {
  RegraAprovacao,
  TipoContexto,
  VotacaoCadastro,
  VotacaoResponse,
} from "../../entities/entities";
import { cadastrarVotacao } from "../../services/apiFacade";
import { toast } from "sonner";

export type VotacaoFormValues = {
  nome: string;
  descricao: string;
  regraAprovacao: string;
  percentualMinimo: string;
  quorumMinimo: string;
  dataInicio: string;
  dataFim: string;
  pauta: string;
  participantesAptos: string;
  pesoVoto: string;
  unidadeApartamento: string;
  categoriaSegmento: string;
  elegibilidade: string;
};

export type VotacaoFormErrors = Partial<Record<keyof VotacaoFormValues, string>>;

export const camposEspecificosPorTipoContexto: Record<
  TipoContexto,
  readonly ("pauta" | "participantesAptos" | "pesoVoto" | "unidadeApartamento" | "categoriaSegmento" | "elegibilidade")[]
> = {
  ASSEMBLEIA: ["pauta", "participantesAptos"],
  CONDOMINIO: ["pesoVoto", "unidadeApartamento"],
  CENTRO_ACADEMICO: ["categoriaSegmento", "elegibilidade"],
};

const regrasAprovacao: RegraAprovacao[] = [
  "MAIORIA_SIMPLES",
  "PERCENTUAL_MINIMO",
];

export function validarVotacaoForm(
  tipoContexto: TipoContexto,
  valores: VotacaoFormValues,
): VotacaoFormErrors {
  const erros: VotacaoFormErrors = {};
  if (!valores.nome.trim()) erros.nome = "Informe o nome.";
  if (!valores.descricao.trim()) erros.descricao = "Informe a descrição.";
  if (!regrasAprovacao.includes(valores.regraAprovacao as RegraAprovacao)) {
    erros.regraAprovacao = "Selecione a regra de aprovação.";
  }

  if (valores.regraAprovacao === "PERCENTUAL_MINIMO") {
    const percentual = Number(valores.percentualMinimo);
    if (!valores.percentualMinimo.trim() || !Number.isFinite(percentual) || percentual <= 0 || percentual > 100) {
      erros.percentualMinimo = "Informe um percentual maior que 0 e até 100.";
    }
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

  if (tipoContexto === "ASSEMBLEIA") {
    if (!valores.pauta.trim()) erros.pauta = "Informe a pauta.";
    const participantes = Number(valores.participantesAptos);
    if (!Number.isSafeInteger(participantes) || participantes <= 0) {
      erros.participantesAptos = "Informe uma quantidade inteira positiva de participantes aptos.";
    }
  } else if (tipoContexto === "CONDOMINIO") {
    const peso = Number(valores.pesoVoto);
    if (!valores.pesoVoto.trim() || !Number.isFinite(peso) || peso <= 0) {
      erros.pesoVoto = "Informe um peso de voto positivo.";
    }
    if (!valores.unidadeApartamento.trim()) {
      erros.unidadeApartamento = "Informe a unidade ou apartamento.";
    }
  } else {
    if (!valores.categoriaSegmento.trim()) {
      erros.categoriaSegmento = "Informe a categoria ou segmento.";
    }
    if (!valores.elegibilidade.trim()) {
      erros.elegibilidade = "Informe a elegibilidade.";
    }
  }

  return erros;
}

function dataIsoLocal(valor: string): string {
  return valor.length === 16 ? `${valor}:00` : valor;
}

export function criarCadastroVotacao(
  contextoId: number,
  tipoContexto: TipoContexto,
  valores: VotacaoFormValues,
): VotacaoCadastro {
  if (!regrasAprovacao.includes(valores.regraAprovacao as RegraAprovacao)) {
    throw new Error("Selecione uma regra de aprovação válida.");
  }

  const camposComuns = {
    contextoId,
    titulo: valores.nome.trim(),
    descricao: valores.descricao.trim(),
    regraAprovacao: valores.regraAprovacao as RegraAprovacao,
    percentualMinimo: valores.regraAprovacao === "PERCENTUAL_MINIMO"
      ? Number(valores.percentualMinimo)
      : null,
    quorumMinimo: Number(valores.quorumMinimo),
    dataInicio: dataIsoLocal(valores.dataInicio),
    dataFim: dataIsoLocal(valores.dataFim),
  };

  switch (tipoContexto) {
    case "ASSEMBLEIA":
      return {
        ...camposComuns,
        pauta: valores.pauta.trim(),
        participantesAptos: Number(valores.participantesAptos),
      };
    case "CONDOMINIO":
      return {
        ...camposComuns,
        pesoVoto: Number(valores.pesoVoto),
        unidadeApartamento: valores.unidadeApartamento.trim(),
      };
    case "CENTRO_ACADEMICO":
      return {
        ...camposComuns,
        categoriaSegmento: valores.categoriaSegmento.trim(),
        elegibilidade: valores.elegibilidade.trim(),
      };
  }
}

const valoresIniciais: VotacaoFormValues = {
  nome: "",
  descricao: "",
  regraAprovacao: "",
  percentualMinimo: "",
  quorumMinimo: "",
  dataInicio: "",
  dataFim: "",
  pauta: "",
  participantesAptos: "",
  pesoVoto: "",
  unidadeApartamento: "",
  categoriaSegmento: "",
  elegibilidade: "",
};

type Props = {
  contextoId: number;
  tipoContexto: TipoContexto;
};

export default function VotacaoForm({ contextoId, tipoContexto }: Props) {
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
    const errosValidacao = validarVotacaoForm(tipoContexto, valores);
    setErros(errosValidacao);
    setErroApi(null);
    setVotacaoCriada(null);
    if (Object.keys(errosValidacao).length > 0) return;

    setEnviando(true);
    try {
      const resposta = await cadastrarVotacao(criarCadastroVotacao(contextoId, tipoContexto, valores));
      setVotacaoCriada(resposta);
      toast.success("Votação cadastrada com sucesso!")
    } catch (erro) {
      setErroApi(erro instanceof Error ? erro.message : "Não foi possível cadastrar a votação.");
      toast.error("Não foi possível cadastrar a votação!")
    } finally {
      setEnviando(false);
    }
  }

  function campoTexto(campo: "nome" | "descricao" | "pauta" | "unidadeApartamento" | "categoriaSegmento" | "elegibilidade", rotulo: string, multiline = false) {
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
        className="rounded-md border border-indigo-500 p-2 outline-none focus:border-purple-600"
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
        className="rounded-md border border-indigo-500 p-2 outline-none focus:border-purple-600"
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

  function campoNumero(
    campo: "percentualMinimo" | "quorumMinimo" | "participantesAptos" | "pesoVoto",
    rotulo: string,
  ) {
    const percentual = campo === "percentualMinimo" || campo === "quorumMinimo";
    return (
      <label htmlFor={campo} className="flex flex-col gap-1 font-medium text-gray-800">
        {rotulo}{percentual ? " (%)" : ""}
        <input
          id={campo}
          name={campo}
          type="number"
          min={percentual ? "0" : campo === "participantesAptos" ? "1" : "0"}
          max={percentual ? "100" : undefined}
          step={campo === "participantesAptos" ? "1" : percentual ? "0.01" : "any"}
          required
          value={valores[campo]}
          onChange={(evento) => atualizarCampo(campo, evento.target.value)}
          aria-invalid={Boolean(erros[campo])}
          aria-describedby={erros[campo] ? `${campo}-erro` : undefined}
          className="rounded-md border border-indigo-500 p-2 outline-none focus:border-purple-600"
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
          className="rounded-md border border-indigo-500 p-2 outline-none focus:border-purple-600"
        />
        {erros[campo] && <span id={`${campo}-erro`} className="text-sm font-normal text-red-700">{erros[campo]}</span>}
      </label>
    );
  }

  const camposEspecificos = camposEspecificosPorTipoContexto[tipoContexto];

  return (
    <section className="flex justify-center items-center">
    <form onSubmit={enviarFormulario} noValidate className="flex w-full max-w-3xl border-indigo-500 border-r-8 border-r-indigo-500 flex-col gap-5 rounded-xl border px-8 py-10 shadow-md">
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
          className="rounded-md border border-indigo-500 p-2 outline-none focus:border-indigo-600"
        >
          <option value="">Selecione uma regra</option>
          <option value="MAIORIA_SIMPLES">Maioria simples</option>
          <option value="PERCENTUAL_MINIMO">Percentual mínimo</option>
        </select>
        {erros.regraAprovacao && <span id="regraAprovacao-erro" className="text-sm font-normal text-red-700">{erros.regraAprovacao}</span>}
      </label>

      {valores.regraAprovacao === "PERCENTUAL_MINIMO" && campoNumero("percentualMinimo", "Percentual mínimo")}
      {campoNumero("quorumMinimo", "Quórum mínimo")}

      {camposEspecificos.includes("pauta") && campoTexto("pauta", "Pauta")}
      {camposEspecificos.includes("participantesAptos") && campoNumero("participantesAptos", "Participantes aptos a votar")}
      {camposEspecificos.includes("pesoVoto") && campoNumero("pesoVoto", "Peso do voto")}
      {camposEspecificos.includes("unidadeApartamento") && campoTexto("unidadeApartamento", "Unidade/apartamento")}
      {camposEspecificos.includes("categoriaSegmento") && campoTexto("categoriaSegmento", "Categoria/segmento")}
      {camposEspecificos.includes("elegibilidade") && campoTexto("elegibilidade", "Elegibilidade", true)}

      {campoData("dataInicio", "Data e hora de início")}
      {campoData("dataFim", "Data e hora de fim")}

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
