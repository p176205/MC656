import type { Usuario, VotacaoResponse } from "@/entities/entities";

// Fachada (Facade) para a API REST do backend.
// As páginas chamam apenas as funções deste módulo e não precisam conhecer
// a URL base, a rota, os headers nem a serialização JSON da requisição.

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export type ResultadoApi = {
  ok: boolean;
  status: number;
};

// POST /api/usuarios
export async function cadastrarUsuario(usuario: Usuario): Promise<ResultadoApi> {
  const resposta = await fetch(`${API_URL}/api/usuarios`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(usuario),
  });

  return { ok: resposta.ok, status: resposta.status };
}

// GET /api/votacoes/contexto/{contextoId}
export async function buscarVotacoesPorContexto(contextoId: number): Promise<VotacaoResponse[]> {
  const resposta = await fetch(`${API_URL}/api/votacoes/contexto/${contextoId}`, {
    method: "GET",
  });

  if (!resposta.ok) {
    throw new Error(`Falha ao buscar votações: HTTP ${resposta.status}`);
  }

  return (await resposta.json()) as VotacaoResponse[];
}