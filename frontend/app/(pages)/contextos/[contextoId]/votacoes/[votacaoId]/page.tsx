"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import type { ApuracaoResponse, VotacaoResponse } from "@/entities/entities";
import { buscarApuracao, buscarVotacaoPorId } from "@/services/apiFacade";
import PainelPropostas from "@/components/votacoes/PainelPropostas";
import {
  buscarApuracao,
  buscarVotacaoPorId,
  getIdUser,
} from "@/services/apiFacade";
import PainelVoto from "@/components/votacoes/PainelVoto";
import { MdArrowBack } from "react-icons/md";

const resultadoLabels: Record<string, string> = {
  APROVADA: "Aprovada",
  REJEITADA: "Rejeitada",
  SEM_QUORUM: "Sem quórum",
  ELEITA: "Chapa eleita",
  EMPATE: "Empate",
};

function formatarData(data: string | null): string {
  if (!data) return "Não definida";

  const dataObj = new Date(data);

  if (Number.isNaN(dataObj.getTime())) return data;

  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(dataObj);
}

export default function VotacaoDetalhesPage() {
  const {
    contextoId: contextoIdParam,
    votacaoId: votacaoIdParam,
  } = useParams<{
    contextoId: string;
    votacaoId: string;
  }>();

  const votacaoId = Number(votacaoIdParam);

  const [votacao, setVotacao] = useState<VotacaoResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [apuracao, setApuracao] = useState<ApuracaoResponse | null>(null);
  const [erroApuracao, setErroApuracao] = useState<string | null>(null);
  const [mostrarPropostas, setMostrarPropostas] = useState(false);
  const [usuarioId, setUsuarioId] = useState<number | null>(null);

  useEffect(() => {
    let cancelado = false;

    getIdUser()
      .then((usuario) => {
        if (!cancelado) {
          setUsuarioId(usuario.id);
        }
      })
      .catch(() => {
        if (!cancelado) {
          setUsuarioId(null);
        }
      });

    return () => {
      cancelado = true;
    };
  }, []);

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

        const prazoExpirado =
          resultado.dataFim !== null &&
          new Date(resultado.dataFim) <= new Date();

        if (!cancelado) {
          setVotacao(resultado);

          if (resultado.status === "ENCERRADA" || prazoExpirado) {
            try {
              const resultadoApuracao = await buscarApuracao(votacaoId);

              if (!cancelado) {
                setApuracao(resultadoApuracao);
              }
            } catch (erroApi) {
              if (!cancelado) {
                setErroApuracao(
                  erroApi instanceof Error
                    ? erroApi.message
                    : "Não foi possível carregar a apuração.",
                );
              }
            }
          }
        }
      } catch (erroApi) {
        if (!cancelado) {
          setErro(
            erroApi instanceof Error
              ? erroApi.message
              : "Não foi possível carregar a votação.",
          );
        }
      } finally {
        if (!cancelado) {
          setCarregando(false);
        }
      }
    }

    void carregarDetalhes();

    return () => {
      cancelado = true;
    };
  }, [votacaoId]);

  const prazoExpirado =
    votacao?.dataFim !== null &&
    votacao?.dataFim !== undefined &&
    new Date(votacao.dataFim) <= new Date();

  const votacaoEncerrada =
    votacao?.status === "ENCERRADA" || prazoExpirado;

  return (
    <main className="mx-auto w-full max-w-4xl px-6 py-10">
      <header className="mt-4 mb-8 flex flex-row justify-between">
        <h1 className="mt-1 text-3xl font-bold text-gray-900">
          Detalhes da {votacao?.titulo}
        </h1>

        <div className="mb-4">
          <Link
            className="inline-flex items-center gap-2 rounded-xl bg-indigo-300 px-4 py-2 font-medium text-indigo-800 hover:bg-indigo-200 hover:shadow-sm"
            href="/home"
          >
            <MdArrowBack size={20} />
            Voltar
          </Link>
        </div>
      </header>

      {carregando && (
        <p role="status" className="mt-6 text-gray-600">
          Carregando votação...
        </p>
      )}

      {!carregando && erro && (
        <p
          role="alert"
          className="mt-6 rounded-lg border border-red-200 bg-red-50 p-5 text-red-800"
        >
          {erro}
        </p>
      )}

      {!carregando && votacao && (
        <section className="mt-6 flex flex-col rounded-xl border border-indigo-500 border-r-8 border-r-indigo-500 bg-white p-8 shadow-sm">
          <header className="flex flex-wrap items-start justify-between gap-3">
            <div className="flex w-full flex-col gap-1">
              <p className="line-clamp-1 text-xl font-semibold text-indigo-950">
                Nome: {votacao.titulo}
              </p>

              <div className="flex items-center justify-between gap-3">
                <h4 className="font-semibold text-gray-600">
                  Contexto: {contextoIdParam}
                </h4>

                <span className="flex shrink-0 items-center gap-2 rounded-full border-2 border-indigo-500 px-3 py-1 text-xs font-medium">
                  <span className="text-gray-800">{votacao.status}</span>

                  <span
                    className={`h-2 w-2 rounded-full ${
                      votacaoEncerrada
                        ? "bg-red-500"
                        : "bg-green-500"
                    }`}
                  />
                </span>
              </div>

              <p className="mt-4 whitespace-pre-wrap border-b-2 border-b-indigo-500 pb-4 text-gray-700">
                Descrição: {votacao.descricao}
              </p>
            </div>
          </header>

          <dl className="mt-6 grid w-full gap-4 text-sm sm:grid-cols-2">
            <div>
              <dt className="font-medium text-gray-800">
                Regra de aprovação
              </dt>
              <dd className="text-gray-600">{votacao.regraAprovacao}</dd>
            </div>

            <div>
              <dt className="font-medium text-gray-800">
                Percentual mínimo
              </dt>
              <dd className="text-gray-600">
                {votacao.percentualMinimo === null
                  ? "Não se aplica"
                  : `${votacao.percentualMinimo}%`}
              </dd>
            </div>

            <div>
              <dt className="font-medium text-gray-800">
                Quórum mínimo
              </dt>
              <dd className="text-gray-600">
                {votacao.quorumMinimo}%
              </dd>
            </div>

            <div>
              <dt className="font-medium text-gray-800">
                Data e hora de início
              </dt>
              <dd className="text-gray-600">
                {formatarData(votacao.dataInicio)}
              </dd>
            </div>

            <div>
              <dt className="font-medium text-gray-800">
                Data e hora de fim
              </dt>
              <dd className="text-gray-600">
                {formatarData(votacao.dataFim)}
              </dd>
            </div>

            <div>
              <dt className="font-medium text-gray-800">Modalidade</dt>
              <dd className="text-gray-600">
                {votacao.tipo === "ELEICAO"
                  ? "Eleição"
                  : "Deliberação"}
              </dd>
            </div>
          </dl>
          {votacao.tipo === "ELEICAO" && (
            <section className="mt-6 border-t pt-6">
              <button
                type="button"
                aria-expanded={mostrarPropostas}
                aria-controls="painel-propostas"
                onClick={() => setMostrarPropostas((mostrar) => !mostrar)}
                className="rounded-lg bg-indigo-600 px-4 py-2 font-medium text-white hover:bg-indigo-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-700"
              >
                {mostrarPropostas ? "Ocultar propostas" : "Ver propostas"}
              </button>
              {mostrarPropostas && (
                <PainelPropostas votacaoId={votacao.id} />
              )}
            </section>
          )}
          {votacao.status === "ENCERRADA" && (

          {votacao.tipo === "ELEICAO" &&
            votacao.chapas &&
            votacao.chapas.length > 0 && (
              <section className="mt-6 border-t pt-6">
                <h2 className="text-lg font-semibold text-gray-900">
                  Chapas concorrentes
                </h2>

                <div className="mt-3 grid gap-3 sm:grid-cols-2">
                  {votacao.chapas.map((chapa) => (
                    <div
                      key={chapa.id}
                      className="rounded-lg border border-indigo-200 bg-indigo-50/40 p-4"
                    >
                      <h3 className="font-semibold text-indigo-950">
                        {chapa.nome}
                      </h3>

                      {chapa.proposta && (
                        <p className="mt-1 whitespace-pre-wrap text-sm text-gray-700">
                          {chapa.proposta}
                        </p>
                      )}

                      <p className="mt-2 text-xs text-gray-600">
                        Membros:{" "}
                        {chapa.membros.map((membro) => membro.nome).join(", ")}
                      </p>
                    </div>
                  ))}
                </div>
              </section>
            )}

          {votacao.status === "ABERTA" &&
            !prazoExpirado &&
            votacao.tipo === "DELIBERACAO" &&
            usuarioId !== null && (
              <PainelVoto
                votacaoId={votacao.id}
                usuarioId={usuarioId}
              />
            )}

          {votacaoEncerrada && (
            <section
              aria-labelledby="apuracao-titulo"
              className="mt-8 border-t pt-6"
            >
              <h2
                id="apuracao-titulo"
                className="text-xl font-semibold text-gray-900"
              >
                Apuração
              </h2>

              {erroApuracao && (
                <p role="alert" className="mt-3 text-red-700">
                  {erroApuracao}
                </p>
              )}
              {!erroApuracao && !apuracao && (
                <p role="status" className="mt-3 text-gray-600">
                  Carregando apuração...
                </p>
              )}
              {apuracao && (
                <dl className="mt-4 grid gap-4 text-sm sm:grid-cols-2">
                  <div>
                    <dt className="font-medium text-gray-800">Resultado</dt>
                    <dd className="text-gray-600">
                      {resultadoLabels[apuracao.resultado] ?? apuracao.resultado}
                    </dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">
                      Quórum atingido
                    </dt>
                    <dd className="text-gray-600">
                      {apuracao.quorumAtingido ? "Sim" : "Não"}
                    </dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">Votos sim</dt>
                    <dd className="text-gray-600">{apuracao.votosSim}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">Votos não</dt>
                    <dd className="text-gray-600">{apuracao.votosNao}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">Abstenções</dt>
                    <dd className="text-gray-600">{apuracao.abstencoes}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">
                      Total de votantes
                    </dt>
                    <dd className="text-gray-600">{apuracao.totalVotantes}</dd>
                  </div>
                  <div>
                    <dt className="font-medium text-gray-800">
                      Percentual de votos sim
                    </dt>
                    <dd className="text-gray-600">
                      {apuracao.percentualSimObtido === null
                        ? "Não disponível"
                        : `${apuracao.percentualSimObtido}%`}
                    </dd>
                  </div>
                </dl>
              )}
            </section>
          )}
        </section>
      )}
    </main>
  );
}