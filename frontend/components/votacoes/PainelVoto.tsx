"use client";

import { useState } from "react";
import type { OpcaoVoto } from "@/entities/entities";
import { votarEmVotacao } from "@/services/apiFacade";

const opcoes: { valor: OpcaoVoto; rotulo: string }[] = [
  { valor: "SIM", rotulo: "Sim" },
  { valor: "NAO", rotulo: "Não" },
  { valor: "ABSTENCAO", rotulo: "Abstenção" },
];

type Props = { votacaoId: number; usuarioId: number };

export default function PainelVoto({ votacaoId, usuarioId }: Props) {
  const [enviando, setEnviando] = useState(false);
  const [sucesso, setSucesso] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function votar(opcao: OpcaoVoto) {
    setEnviando(true);
    setErro(null);
    try {
      await votarEmVotacao(votacaoId, { usuarioId, opcao });
      setSucesso(true);
    } catch (e) {
      setErro(e instanceof Error ? e.message : "Não foi possível registrar o voto.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <section id="votar" aria-labelledby="votar-titulo" className="mt-8">
      <h2 id="votar-titulo" className="text-xl font-semibold text-gray-900">Seu voto</h2>
      {sucesso ? (
        <p role="status" className="mt-3 text-green-800">Voto registrado.</p>
      ) : (
        <div className="mt-3 flex gap-3">
          {opcoes.map(({ valor, rotulo }) => (
            <button key={valor} type="button" disabled={enviando} onClick={() => votar(valor)}
              className="rounded-lg border border-indigo-300 bg-white px-4 py-2 font-medium text-indigo-700 hover:bg-indigo-50 disabled:opacity-50">
              {rotulo}
            </button>
          ))}
        </div>
      )}
      {erro && <p role="alert" className="mt-3 text-red-800">{erro}</p>}
    </section>
  );
}
