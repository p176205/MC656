"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import type { VotacaoResponse } from "@/entities/entities";
import { buscarVotacaoPorId } from "@/services/apiFacade";

const statusLabels: Record<string, string> = {
  AGENDADA: "Agendada",
  ABERTA: "Aberta",
  EM_ANDAMENTO: "Em andamento",
  ENCERRADA: "Encerrada",
  CANCELADA: "Cancelada",
};

function formatarData(data: string): string {
  const dataObj = new Date(data);
  if (Number.isNaN(dataObj.getTime())) return data;
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(dataObj);
}

export default function VotacaoDetalhesPage() {
  const { contextoId: contextoIdParam, votacaoId: votacaoIdParam } =
    useParams<{ contextoId: string; votacaoId: string }>();
  const votacaoId = Number(votacaoIdParam);
  const [votacao, setVotacao] = useState<VotacaoResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;

    async function carregarDetalhes() {
      if (!Number.isSafeInteger(votacaoId) || votacaoId <= 0) {
        setErro("O identificador da votação é inválido.");
        setCarregando(false);
        return;
      }

      try {
        const resultado = await buscarVotacaoPorId(votacaoId);
        if (!cancelado) {
          setVotacao(resultado);
        }
      } catch (erroApi) {
        if (!cancelado) {
          setErro(erroApi instanceof Error ? erroApi.message : "Não foi possível carregar a votação.");
        }
      } finally {
        if (!cancelado) setCarregando(false);
      }
    }

    void carregarDetalhes();
    return () => {
      cancelado = true;
    };
  }, [votacaoId]);

  return (
    <main className="mx-auto w-full max-w-4xl px-6 py-10">
      <Link
        href={`/contextos/${contextoIdParam}/votacoes`}
        className="text-sm font-medium text-purple-700 hover:text-purple-900"
      >
        ← Voltar para votações
      </Link>

      {carregando && <p role="status" className="mt-6 text-gray-600">Carregando votação...</p>}
      {!carregando && erro && (
        <p role="alert" className="mt-6 rounded-lg border border-red-200 bg-red-50 p-5 text-red-800">{erro}</p>
      )}
      {!carregando && votacao && (
        <article className="mt-6 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
          <header className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <p className="text-sm font-medium text-purple-700">Contexto {contextoIdParam}</p>
              <h1 className="mt-1 text-3xl font-bold text-gray-900">{votacao.nome}</h1>
            </div>
            <span className="rounded-full bg-purple-100 px-3 py-1 text-sm font-medium text-purple-800">
              {statusLabels[votacao.status] ?? votacao.status}
            </span>
          </header>

          <p className="mt-4 whitespace-pre-wrap text-gray-700">{votacao.descricao}</p>

          <dl className="mt-6 grid gap-4 text-sm sm:grid-cols-2">
            <div>
              <dt className="font-medium text-gray-800">Regra de aprovação</dt>
              <dd className="text-gray-600">{votacao.regraAprovacao}</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-800">Percentual mínimo</dt>
              <dd className="text-gray-600">{votacao.percentualMinimo}%</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-800">Quórum mínimo</dt>
              <dd className="text-gray-600">{votacao.quorumMinimo}%</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-800">Data e hora de início</dt>
              <dd className="text-gray-600">{formatarData(votacao.dataInicio)}</dd>
            </div>
            <div>
              <dt className="font-medium text-gray-800">Data e hora de fim</dt>
              <dd className="text-gray-600">{formatarData(votacao.dataFim)}</dd>
            </div>
            {votacao.pauta && (
              <div>
                <dt className="font-medium text-gray-800">Pauta</dt>
                <dd className="text-gray-600">{votacao.pauta}</dd>
              </div>
            )}
            {votacao.participantesAptos !== null && (
              <div>
                <dt className="font-medium text-gray-800">Participantes aptos a votar</dt>
                <dd className="text-gray-600">{votacao.participantesAptos}</dd>
              </div>
            )}
            {votacao.pesoVoto !== null && (
              <div>
                <dt className="font-medium text-gray-800">Peso do voto</dt>
                <dd className="text-gray-600">{votacao.pesoVoto}</dd>
              </div>
            )}
            {votacao.unidadeApartamento && (
              <div>
                <dt className="font-medium text-gray-800">Unidade/apartamento</dt>
                <dd className="text-gray-600">{votacao.unidadeApartamento}</dd>
              </div>
            )}
            {votacao.categoriaSegmento && (
              <div>
                <dt className="font-medium text-gray-800">Categoria/segmento</dt>
                <dd className="text-gray-600">{votacao.categoriaSegmento}</dd>
              </div>
            )}
            {votacao.elegibilidade && (
              <div>
                <dt className="font-medium text-gray-800">Elegibilidade</dt>
                <dd className="whitespace-pre-wrap text-gray-600">{votacao.elegibilidade}</dd>
              </div>
            )}
          </dl>
        </article>
      )}
    </main>
  );
}
