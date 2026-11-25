"use client";

import { useState } from "react";
import type {
  ChapaResponse,
  OpcaoVoto,
  TipoVotacao,
  VotoCadastro,
} from "@/entities/entities";
import { votarEmVotacao } from "@/services/apiFacade";
import { MdHowToVote, MdOutlineSecurity } from "react-icons/md";

const opcoesDeliberacao: { valor: OpcaoVoto; rotulo: string }[] = [
  { valor: "SIM", rotulo: "Sim" },
  { valor: "NAO", rotulo: "Não" },
  { valor: "ABSTENCAO", rotulo: "Abstenção" },
];

type Props = {
  votacaoId: number;
  usuarioId: number;
  tipo?: TipoVotacao;
  chapas?: ChapaResponse[];
};

export default function PainelVoto({
  votacaoId,
  usuarioId,
  tipo = "DELIBERACAO",
  chapas = [],
}: Props) {
  const [enviando, setEnviando] = useState(false);
  const [sucesso, setSucesso] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function registrarVoto(voto: VotoCadastro) {
    setEnviando(true);
    setErro(null);
    try {
      await votarEmVotacao(votacaoId, voto);
      setSucesso(true);
    } catch (e) {
      setErro(
        e instanceof Error ? e.message : "Não foi possível registrar o voto.",
      );
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section
      id="votar"
      aria-labelledby="votar-titulo"
      className="mt-8 border-t pt-6"
    >
      <h2 id="votar-titulo" className="text-xl font-semibold text-gray-900">
        Votar
      </h2>

      <div className="mt-3 grid grid-cols-2 gap-4 border-t border-indigo-200 pt-3">
        <div className="flex items-start gap-2">
          <MdOutlineSecurity
            size={20}
            className="mt-0.5 shrink-0 text-indigo-600"
          />

          <div>
            <p className="text-sm font-medium text-indigo-900">
              Votação anônima
            </p>
            <p className="text-sm text-gray-600">
              Sua escolha é sigilosa e não será identificada individualmente.
            </p>
          </div>
        </div>

        <div className="flex items-start gap-2">
          <MdHowToVote size={20} className="mt-0.5 shrink-0 text-indigo-600" />

          <div>
            <p className="text-sm font-medium text-indigo-900">
              Registro de voto
            </p>
            <p className="text-sm text-gray-600">
              Selecione uma das opções disponíveis para registrar seu voto.
            </p>
          </div>
        </div>
      </div>
      {sucesso ? (
        <p role="status" className="mt-3 text-green-800">
          Voto registrado.
        </p>
      ) : tipo === "ELEICAO" ? (
        <div className="mt-3 flex flex-col gap-3">
          <p className="text-sm text-gray-600">
            Selecione uma das chapas concorrentes ou registre abstenção:
          </p>
          <div className="flex flex-wrap gap-3">
            {chapas.map((chapa) => (
              <button
                key={chapa.id}
                type="button"
                disabled={enviando}
                onClick={() => registrarVoto({ usuarioId, chapaId: chapa.id })}
                className="rounded-lg border border-indigo-300 bg-white px-4 py-2 font-medium text-indigo-700 hover:bg-indigo-50 disabled:opacity-50"
              >
                {chapa.nome}
              </button>
            ))}
            <button
              type="button"
              disabled={enviando}
              onClick={() => registrarVoto({ usuarioId, opcao: "ABSTENCAO" })}
              className="rounded-lg border border-gray-300 bg-white px-4 py-2 font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
            >
              Abstenção
            </button>
          </div>
        </div>
      ) : (
        <div className="mt-3 flex flex-wrap gap-3">
          {opcoesDeliberacao.map(({ valor, rotulo }) => (
            <button
              key={valor}
              type="button"
              disabled={enviando}
              onClick={() => registrarVoto({ usuarioId, opcao: valor })}
              className="rounded-lg border border-indigo-300 bg-white px-4 py-2 font-medium text-indigo-700 hover:bg-indigo-50 disabled:opacity-50"
            >
              {rotulo}
            </button>
          ))}
        </div>
      )}
      {erro && (
        <p role="alert" className="mt-3 text-red-800">
          {erro}
        </p>
      )}
    </section>
  );
}
