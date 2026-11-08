"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import VotacaoForm from "@/components/votacoes/VotacaoForm";

export default function NovaVotacaoPage() {
  const { contextoId: contextoIdParam } = useParams<{ contextoId: string }>();
  const contextoId = Number(contextoIdParam);

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

      {Number.isSafeInteger(contextoId) && contextoId > 0 ? (
        <VotacaoForm contextoId={contextoId} />
      ) : (
        <p role="alert" className="rounded-lg border border-red-200 bg-red-50 p-5 text-red-800">
          O identificador do contexto é inválido.
        </p>
      )}
    </main>
  );
}
