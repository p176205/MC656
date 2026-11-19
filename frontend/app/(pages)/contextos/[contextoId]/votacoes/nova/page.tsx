"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import type { ContextoDetalhe, MembroChapa } from "@/entities/entities";
import {
  buscarContexto,
  buscarMembrosDoContexto,
  getIdUser,
} from "@/services/apiFacade";
import VotacaoForm from "@/components/votacoes/VotacaoForm";
import { MdArrowBack } from "react-icons/md";

export default function NovaVotacaoPage() {
  const { contextoId: contextoIdParam } = useParams<{ contextoId: string }>();
  const contextoId = Number(contextoIdParam);
  const [contexto, setContexto] = useState<ContextoDetalhe | null>(null);
  const [membros, setMembros] = useState<MembroChapa[]>([]);
  const [adminId, setAdminId] = useState<number | undefined>(undefined);
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
        const [resultadoContexto, resultadoMembros, usuarioAtual] =
          await Promise.all([
            buscarContexto(contextoId),
            buscarMembrosDoContexto(contextoId).catch(() => []),
            getIdUser().catch(() => null),
          ]);
        if (!cancelado) {
          setContexto(resultadoContexto);
          setMembros(resultadoMembros);
          if (usuarioAtual) setAdminId(usuarioAtual.id);
        }
      } catch (erroApi) {
        if (!cancelado) {
          setErro(
            erroApi instanceof Error
              ? erroApi.message
              : "Não foi possível carregar o contexto.",
          );
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
    <main className="mx-auto w-full max-w-3xl px-6 py-10">
      <header className="flex flex-row mb-8 mt-4 justify-between">
        <h1 className="mt-1 text-3xl font-bold text-gray-900">{contexto?.tipo != "ASSEMBLEIA" ? "Nova votação" : "Nova deliberação"}</h1>
        <Link
          className="inline-flex items-center gap-2 text-indigo-800 bg-indigo-300 py-2 px-4 rounded-xl font-medium hover:bg-indigo-200 hover:shadow-sm"
          href="/home"
        >
          <MdArrowBack size={20} />
          Voltar
        </Link>
      </header>

      {carregando && <p role="status">Carregando contexto...</p>}
      {!carregando && erro && (
        <p
          role="alert"
          className="rounded-lg border border-red-200 bg-red-50 p-5 text-red-800"
        >
          {erro}
        </p>
      )}
      {!carregando && contexto && (
        <VotacaoForm
          contextoId={contexto.id}
          tipoContexto={contexto.tipo}
          adminId={adminId}
          membrosContexto={membros}
        />
      )}
    </main>
  );
}
