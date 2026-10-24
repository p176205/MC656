import { afterEach, describe, expect, it, vi } from "vitest";
import { cadastrarUsuario } from "../services/apiFacade";

const usuario = { nome: "Maria", email: "maria@email.com", senha: "123456" };

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("cadastrarUsuario (fachada da API)", () => {
  it("envia POST com JSON para /api/usuarios e retorna ok quando o backend responde 201", async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 201, text: vi.fn().mockResolvedValue("Usuário cadastrado com sucesso") });
    vi.stubGlobal("fetch", fetchMock);

    const resultado = await cadastrarUsuario(usuario);

    expect(resultado).toEqual({ ok: true, status: 201, message: "Usuário cadastrado com sucesso" });
    expect(fetchMock).toHaveBeenCalledTimes(1);

    const [url, opcoes] = fetchMock.mock.calls[0];
    expect(url).toMatch(/\/api\/usuarios$/);
    expect(opcoes.method).toBe("POST");
    expect(opcoes.headers).toEqual({ "Content-Type": "application/json" });
    expect(opcoes.credentials).toBe("include");
    expect(JSON.parse(opcoes.body)).toEqual(usuario);
  });

  it("retorna ok=false quando o backend recusa o cadastro (400)", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 400, text: vi.fn().mockResolvedValue("E-mail já cadastrado"), }));

    const resultado = await cadastrarUsuario(usuario);

    expect(resultado).toEqual({ ok: false, status: 400, message: "E-mail já cadastrado", });
  });

  it("propaga o erro quando a rede falha", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("rede indisponível")));

    await expect(cadastrarUsuario(usuario)).rejects.toThrow("rede indisponível");
  });
});