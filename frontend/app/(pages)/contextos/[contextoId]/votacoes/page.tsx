"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import type { VotacaoResponse } from "@/entities/entities";
import { buscarVotacoesPorContexto } from "@/services/apiFacade";

const statusLabels: Record<string, string> = {
  ABERTA: "Aberta",
  ENCERRADA: "Encerrada",
};

function formatarData(data: string | null): string {
  if (!data) return "Não definida";
  const dataObj = new Date(data);
  if (Number.isNaN(dataObj.getTime())) {
    return data;
  }

  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(dataObj);
}

export default function VotacoesPage() {
  const { contextoId: contextoIdParam } = useParams<{ contextoId: string }>();
  const contextoId = Number(contextoIdParam);
  const [votacoes, setVotacoes] = useState<VotacaoResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    let cancelado = false;

    async function carregarVotacoes() {
      setCarregando(true);
      setErro(null);

      if (!Number.isSafeInteger(contextoId) || contextoId <= 0) {
        setErro("O identificador do contexto é inválido.");
        setCarregando(false);
        return;
      }

      try {
        const resultado = await buscarVotacoesPorContexto(contextoId);
        if (!cancelado) {
          setVotacoes(resultado);
        }
      } catch (erroApi) {
        if (!cancelado) {
          setErro(erroApi instanceof Error ? erroApi.message : "Não foi possível carregar as votações.");
          setVotacoes([]);
        }
      } finally {
        if (!cancelado) {
          setCarregando(false);
        }
      }
    }

    void carregarVotacoes();

    return () => {
      cancelado = true;
    };
  }, [contextoId, tentativa]);

  return (
    <main className="mx-auto w-full max-w-4xl px-6 py-10">
      <header className="mb-8">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-sm font-medium text-purple-700">Contexto {contextoIdParam}</p>
            <h1 className="mt-1 text-3xl font-bold text-gray-900">Votações</h1>
          </div>
          <Link
            href={`/contextos/${contextoIdParam}/votacoes/nova`}
            className="rounded-md bg-purple-700 px-4 py-2 font-medium text-white hover:bg-purple-800"
          >
            Criar votação
          </Link>
        </div>
      </header>

      {carregando && (
        <p role="status" className="text-gray-600">
          Carregando votações...
        </p>
      )}

      {!carregando && erro && (
        <section role="alert" className="rounded-lg border border-red-200 bg-red-50 p-5 text-red-800">
          <p>{erro}</p>
          <button
            type="button"
            className="mt-4 rounded-md bg-red-700 px-4 py-2 font-medium text-white hover:bg-red-800"
            onClick={() => setTentativa((valor) => valor + 1)}
          >
            Tentar novamente
          </button>
        </section>
      )}

      {!carregando && !erro && votacoes.length === 0 && (
        <p className="rounded-lg border border-gray-200 bg-white p-5 text-gray-600">
          Este contexto ainda não tem votações.
        </p>
      )}

      {!carregando && !erro && votacoes.length > 0 && (
        <ul className="space-y-4">
          {votacoes.map((votacao) => (
            <li key={votacao.id} className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
              <Link
                href={`/contextos/${contextoIdParam}/votacoes/${votacao.id}`}
                className="block rounded-md outline-none focus-visible:ring-2 focus-visible:ring-purple-700"
                aria-label={`Ver detalhes da votação ${votacao.titulo}`}
              >
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <h2 className="text-xl font-semibold text-gray-900">{votacao.titulo}</h2>
                  <span className="rounded-full bg-purple-100 px-3 py-1 text-sm font-medium text-purple-800">
                    {statusLabels[votacao.status] ?? votacao.status}
                  </span>
                </div>
                <p className="mt-2 text-gray-700">{votacao.descricao}</p>
                <dl className="mt-4 grid gap-3 text-sm text-gray-600 sm:grid-cols-2">
                  <div>
                    <dt className="font-medium text-gray-800">Regra de aprovação</dt>
                    <dd>{votacao.regraAprovacao}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">Percentual mínimo</dt>
                    <dd>{votacao.percentualMinimo === null ? "Não se aplica" : `${votacao.percentualMinimo}%`}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">Quórum mínimo</dt>
                    <dd>{votacao.quorumMinimo}%</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">Período</dt>
                    <dd>
                      {formatarData(votacao.dataInicio)} – {formatarData(votacao.dataFim)}
                    </dd>
                  </div>
                </dl>
                <span className="mt-4 inline-block text-sm font-medium text-purple-700">Ver detalhes →</span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
