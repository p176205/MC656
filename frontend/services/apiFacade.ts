import type { ContextoDTO, Usuario } from "@/entities/entities";

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


// GET /api/contextos/{id}
// Testando o metodo GET
// Gente no fetch esta hardcoded pq estava apenas testando com um id que esta no meu banco. Mais para frente farei um cookie de sessão para guardar o id do usuario no navegador e vou usa-lo aqui
export async function getContextos(): Promise<Response> {
  const resposta = await fetch(`http://localhost:8080/api/contextos/${3}`, 
    {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    },
  );

  return resposta;
}

