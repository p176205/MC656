import { afterEach, describe, expect, it, vi } from "vitest";
import { buscarVotacoesPorContexto, cadastrarUsuario, cadastrarVotacao } from "../services/apiFacade";

const usuario = { nome: "Maria", email: "maria@email.com", senha: "123456" };
const votacoes = [{
  id: 8,
  contextoId: 12,
  nome: "Eleição de representante",
  descricao: "Escolha do representante.",
  regraAprovacao: "MAIORIA_SIMPLES",
  percentualMinimo: 60,
  quorumMinimo: 25,
  dataInicio: "2026-11-01T10:00:00",
  dataFim: "2026-11-02T10:00:00",
  status: "AGENDADA",
}];

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("cadastrarUsuario (fachada da API)", () => {
  it("envia POST com JSON para /api/usuarios e retorna ok quando o backend responde 201", async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 201 });
    vi.stubGlobal("fetch", fetchMock);

    const resultado = await cadastrarUsuario(usuario);

    expect(resultado).toEqual({ ok: true, status: 201 });
    expect(fetchMock).toHaveBeenCalledTimes(1);

    const [url, opcoes] = fetchMock.mock.calls[0];
    expect(url).toMatch(/\/api\/usuarios$/);
    expect(opcoes.method).toBe("POST");
    expect(opcoes.headers).toEqual({ "Content-Type": "application/json" });
    expect(JSON.parse(opcoes.body)).toEqual(usuario);
  });

  it("retorna ok=false quando o backend recusa o cadastro (400)", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 400 }));

    const resultado = await cadastrarUsuario(usuario);

    expect(resultado).toEqual({ ok: false, status: 400 });
  });

  it("propaga o erro quando a rede falha", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("rede indisponível")));

    await expect(cadastrarUsuario(usuario)).rejects.toThrow("rede indisponível");
  });
});

describe("buscarVotacoesPorContexto (fachada da API)", () => {
  it("envia GET para o contexto e retorna os itens da resposta", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: vi.fn().mockResolvedValue(votacoes),
    });
    vi.stubGlobal("fetch", fetchMock);

    const resultado = await buscarVotacoesPorContexto(12);

    expect(resultado).toEqual(votacoes);
    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, opcoes] = fetchMock.mock.calls[0];
    expect(url).toMatch(/\/api\/votacoes\/contexto\/12$/);
    expect(opcoes.method).toBe("GET");
  });

  it("propaga falhas HTTP e de rede", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 404 }));

    await expect(buscarVotacoesPorContexto(999))
      .rejects.toThrow("Falha ao buscar votações: HTTP 404");

    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("rede indisponível")));
    await expect(buscarVotacoesPorContexto(12)).rejects.toThrow("rede indisponível");
  });
});

describe("cadastrarVotacao (fachada da API)", () => {
  const cadastro = {
    contextoId: 12,
    nome: "Eleição de representante",
    descricao: "Escolha do representante.",
    regraAprovacao: "MAIORIA_SIMPLES" as const,
    percentualMinimo: 60.5,
    quorumMinimo: 25.25,
    dataInicio: "2026-11-01T10:00:00",
    dataFim: "2026-11-02T10:00:00",
    status: "AGENDADA" as const,
  };

  it("envia POST com os dados do contrato e retorna a votação criada", async () => {
    const votacaoCriada = { ...votacoes[0], ...cadastro, id: 21 };
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 201,
      json: vi.fn().mockResolvedValue(votacaoCriada),
    });
    vi.stubGlobal("fetch", fetchMock);

    const resultado = await cadastrarVotacao(cadastro);

    expect(resultado).toEqual(votacaoCriada);
    const [url, opcoes] = fetchMock.mock.calls[0];
    expect(url).toMatch(/\/api\/votacoes$/);
    expect(opcoes.method).toBe("POST");
    expect(opcoes.headers).toEqual({ "Content-Type": "application/json" });
    expect(JSON.parse(opcoes.body)).toEqual(cadastro);
  });

  it("propaga a mensagem de erro da API sem retorno de sucesso", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: false,
      status: 404,
      text: vi.fn().mockResolvedValue(JSON.stringify({ detail: "Contexto não encontrado." })),
    }));

    await expect(cadastrarVotacao(cadastro)).rejects.toThrow("Contexto não encontrado.");
  });
});