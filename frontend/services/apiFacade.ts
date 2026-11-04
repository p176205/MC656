import type { Contexto, ContextoDTO, LoginUsuario, TipoContexto, Usuario, UsuarioResponse } from "@/entities/entities";

// Fachada (Facade) para a API REST do backend.
// As páginas chamam apenas as funções deste módulo e não precisam conhecer
// a URL base, a rota, os headers nem a serialização JSON da requisição.

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export type ResultadoApi = {
  ok: boolean;
  status: number;
  message: string;
};

// POST /api/usuarios
export async function cadastrarUsuario(usuario: Usuario): Promise<ResultadoApi> {
  const resposta = await fetch(`${API_URL}/api/usuarios`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(usuario),
  });

  const message = await resposta.text();
  return { ok: resposta.ok, status: resposta.status, message: message };
}

// POST /api/usuarios/login
export async function loginUsuario(usuario: LoginUsuario): Promise<ResultadoApi> {
  const resposta = await fetch(`${API_URL}/api/usuarios/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(usuario),
  });

  const message = await resposta.text();
  return { ok: resposta.ok, status: resposta.status, message: message };
}

// GET /api/usuarios/id
export async function getIdUser(): Promise<UsuarioResponse> {
  const resposta = await fetch(`${API_URL}/api/usuarios/me`, {
    method: "GET",
    credentials: "include",
  });

  if (!resposta.ok) {
    if (resposta.status === 401) {
      throw new Error("Usuário não autenticado.");
    }

    throw new Error("Erro ao buscar o ID do usuário.");
  }

  const usuarioId: UsuarioResponse = await resposta.json();

  return usuarioId;
}

// POST /api/usuarios/logout
export async function logoutUsuario(): Promise<ResultadoApi> {
  const resposta = await fetch(`${API_URL}/api/usuarios/logout`, {
    method: "POST",
    credentials: "include",
  });

  const message = await resposta.text();
  return { ok: resposta.ok, status: resposta.status, message: message };
}

// POST /api/contextos
export async function criarContexto(contexto: Contexto, tipo: TipoContexto): Promise<ContextoDTO> {
  const rotasPorTipo = {
    ASSEMBLEIA: "assembleias",
    CENTRO_ACADEMICO: "centros-academicos",
    CONDOMINIO: "condominios",
  };

  const endpointTipo = rotasPorTipo[tipo];
  console.log("endpoint",endpointTipo)
  console.log(contexto)

  const resposta = await fetch(`${API_URL}/api/contextos/${endpointTipo}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(contexto), 
  });

  if (!resposta.ok) {
    if (resposta.status === 401) {
      throw new Error("Não foi possivel criar o contexto.");
    }

    throw new Error("Erro ao criar o contexto.");
  }

  const contexto_res = await resposta.json();
  return contexto_res;
}

// GET /api/contextos/{id}
export async function getContextos(usuarioId: number): Promise<Response> {
  const resposta = await fetch(
    `${API_URL}/api/contextos/${usuarioId}`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  return resposta;
}

