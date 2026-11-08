"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import type { ContextoResponse } from "@/entities/entities";
import { buscarContexto } from "@/services/apiFacade";
import VotacaoForm from "@/components/votacoes/VotacaoForm";

export default function NovaVotacaoPage() {
  const { contextoId: contextoIdParam } = useParams<{ contextoId: string }>();
  const contextoId = Number(contextoIdParam);
  const [contexto, setContexto] = useState<ContextoResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;

    async function carregarContexto() {
      if (!Number.isSafeInteger(contextoId) || contextoId <= 0) {
        setErro("O identificador do contexto é inválido.");
        setCarregando(false);
        return;
      }

      try {
        const resultado = await buscarContexto(contextoId);
        if (!cancelado) setContexto(resultado);
      } catch (erroApi) {
        if (!cancelado) {
          setErro(erroApi instanceof Error ? erroApi.message : "Não foi possível carregar o contexto.");
        }
      } finally {
        if (!cancelado) setCarregando(false);
      }
    }

    void carregarContexto();
    return () => {
      cancelado = true;
    };
  }, [contextoId]);

  return (
    <main className="mx-auto w-full max-w-4xl px-6 py-10">
      <Link
        href={`/contextos/${contextoIdParam}/votacoes`}
        className="text-sm font-medium text-purple-700 hover:text-purple-900"
      >
        ← Voltar para votações
      </Link>
      <header className="mb-8 mt-4">
        <p className="text-sm font-medium text-purple-700">Contexto {contextoIdParam}</p>
        <h1 className="mt-1 text-3xl font-bold text-gray-900">Nova votação</h1>
      </header>

      {carregando && <p role="status">Carregando contexto...</p>}
      {!carregando && erro && (
        <p role="alert" className="rounded-lg border border-red-200 bg-red-50 p-5 text-red-800">{erro}</p>
      )}
      {!carregando && contexto && (
        <VotacaoForm contextoId={contexto.id} tipoContexto={contexto.tipo} />
      )}
    </main>
  );
}
