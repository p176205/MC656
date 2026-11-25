"use client";

import { useEffect, useState } from "react";
import type { ChapaResponse } from "@/entities/entities";
import { buscarChapasDaVotacao } from "@/services/apiFacade";

interface PainelPropostasProps {
  votacaoId: number;
}

export default function PainelPropostas({ votacaoId }: PainelPropostasProps) {
  const [chapas, setChapas] = useState<ChapaResponse[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    let cancelado = false;

    async function carregarChapas() {
      try {
        const resultado = await buscarChapasDaVotacao(votacaoId);
        if (!cancelado) setChapas(resultado);
      } catch (erroApi) {
        if (!cancelado) {
          setErro(
            erroApi instanceof Error
              ? erroApi.message
              : "Não foi possível carregar as propostas das chapas.",
          );
        }
      } finally {
        if (!cancelado) setCarregando(false);
      }
    }

    void carregarChapas();
    return () => {
      cancelado = true;
    };
  }, [votacaoId]);

  return (
    <section
      id="painel-propostas"
      aria-labelledby="painel-propostas-titulo"
      className="mt-6 border-t pt-6"
    >
      <h2
        id="painel-propostas-titulo"
        className="text-lg font-semibold text-gray-900"
      >
        Propostas das chapas
      </h2>
      <p className="mt-1 text-sm text-gray-600">
        Confira a proposta cadastrada e os integrantes de cada chapa.
      </p>

      {carregando && (
        <p role="status" className="mt-4 text-gray-600">
          Carregando propostas...
        </p>
      )}
      {!carregando && erro && (
        <p role="alert" className="mt-4 text-red-700">
          {erro}
        </p>
      )}
      {!carregando && !erro && chapas.length === 0 && (
        <p className="mt-4 text-gray-600">
          Esta votação ainda não possui chapas cadastradas.
        </p>
      )}
      {!carregando && !erro && chapas.length > 0 && (
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          {chapas.map((chapa) => (
            <article
              key={chapa.id}
              className="rounded-lg border border-indigo-200 bg-indigo-50/40 p-4"
            >
              <h3 className="text-base font-semibold text-indigo-950">
                {chapa.nome}
              </h3>
              <div className="mt-3">
                <h4 className="text-sm font-medium text-gray-800">
                  Proposta cadastrada
                </h4>
                <p className="mt-1 whitespace-pre-wrap text-sm text-gray-700">
                  {chapa.proposta || "Esta chapa não cadastrou uma proposta."}
                </p>
              </div>
              <div className="mt-3">
                <h4 className="text-sm font-medium text-gray-800">Integrantes</h4>
                {chapa.membros.length > 0 ? (
                  <ul className="mt-1 list-inside list-disc text-sm text-gray-700">
                    {chapa.membros.map((membro) => (
                      <li key={membro.id}>{membro.nome}</li>
                    ))}
                  </ul>
                ) : (
                  <p className="mt-1 text-sm text-gray-700">
                    Nenhum integrante informado.
                  </p>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
