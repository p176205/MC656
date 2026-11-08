import type { Usuario, VotacaoCadastro, VotacaoResponse } from "@/entities/entities";

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

// POST /api/votacoes
export async function cadastrarVotacao(votacao: VotacaoCadastro): Promise<VotacaoResponse> {
  const resposta = await fetch(`${API_URL}/api/votacoes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(votacao),
  });

  if (!resposta.ok) {
    const corpoErro = await resposta.text();
    throw new Error(extrairMensagemErro(corpoErro) ?? `Falha ao cadastrar votação: HTTP ${resposta.status}`);
  }

  return (await resposta.json()) as VotacaoResponse;
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

function extrairMensagemErro(corpo: string): string | null {
  if (!corpo.trim()) {
    return null;
  }

  try {
    const erro = JSON.parse(corpo) as {
      detail?: string;
      message?: string;
      title?: string;
      errors?: string[] | Record<string, string>;
    };

    if (typeof erro.detail === "string" && erro.detail.trim()) return erro.detail;
    if (typeof erro.message === "string" && erro.message.trim()) return erro.message;
    if (typeof erro.title === "string" && erro.title.trim()) return erro.title;
    if (Array.isArray(erro.errors)) return erro.errors.join(" ");
    if (erro.errors && typeof erro.errors === "object") return Object.values(erro.errors).join(" ");
  } catch {
    return corpo;
  }

  return corpo;
}