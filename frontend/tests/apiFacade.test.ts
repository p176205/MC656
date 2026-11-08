import { afterEach, describe, expect, it, vi } from "vitest";
import {
  buscarApuracao,
  buscarContexto,
  buscarVotacaoPorId,
  buscarVotacoesPorContexto,
  cadastrarUsuario,
  cadastrarVotacao,
} from "../services/apiFacade";

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

  describe("fachada de votações", () => {
    const votacao = {
      id: 8,
      titulo: "Assembleia",
      descricao: "Descrição",
      contextoId: 2,
      contextoNome: "Assembleia Geral",
      tipo: "DELIBERACAO",
      regraAprovacao: "MAIORIA_SIMPLES",
      percentualMinimo: null,
      quorumMinimo: 50,
      status: "ABERTA",
      dataInicio: "2026-11-01T10:00:00",
      dataFim: "2026-11-02T10:00:00",
      pauta: "Pauta",
      participantesAptos: 15,
      pesoVoto: null,
      unidadeApartamento: null,
      categoriaSegmento: null,
      elegibilidade: null,
    };

    it("consulta contexto e lista por contexto usando GET", async () => {
      const fetchMock = vi.fn()
        .mockResolvedValueOnce({ ok: true, json: vi.fn().mockResolvedValue({ id: 2, nome: "Assembleia", tipo: "ASSEMBLEIA" }) })
        .mockResolvedValueOnce({ ok: true, json: vi.fn().mockResolvedValue([votacao]) });
      vi.stubGlobal("fetch", fetchMock);

      await expect(buscarContexto(2)).resolves.toEqual({ id: 2, nome: "Assembleia", tipo: "ASSEMBLEIA" });
      await expect(buscarVotacoesPorContexto(2)).resolves.toEqual([votacao]);

      expect(fetchMock.mock.calls[0][0]).toMatch(/\/api\/contextos\/id\/2$/);
      expect(fetchMock.mock.calls[0][1].method).toBe("GET");
      expect(fetchMock.mock.calls[1][0]).toMatch(/\/api\/votacoes\/contexto\/2$/);
      expect(fetchMock.mock.calls[1][1].method).toBe("GET");
    });

    it("busca detalhes e apuração por id", async () => {
      const apuracao = {
        votacaoId: 8, titulo: "Assembleia", resultado: "APROVADA", quorumAtingido: true,
        votosSim: 5, votosNao: 2, abstencoes: 0, totalVotantes: 7,
        regraAprovacao: "MAIORIA_SIMPLES", percentualMinimo: null, percentualSimObtido: 71.43,
      };
      const fetchMock = vi.fn()
        .mockResolvedValueOnce({ ok: true, json: vi.fn().mockResolvedValue(votacao) })
        .mockResolvedValueOnce({ ok: true, json: vi.fn().mockResolvedValue(apuracao) });
      vi.stubGlobal("fetch", fetchMock);

      await expect(buscarVotacaoPorId(8)).resolves.toEqual(votacao);
      await expect(buscarApuracao(8)).resolves.toEqual(apuracao);
      expect(fetchMock.mock.calls[0][0]).toMatch(/\/api\/votacoes\/8$/);
      expect(fetchMock.mock.calls[0][1].method).toBe("GET");
      expect(fetchMock.mock.calls[1][0]).toMatch(/\/api\/votacoes\/8\/apuracao$/);
      expect(fetchMock.mock.calls[1][1].method).toBe("GET");
    });

    it("cadastra payload específico do contexto sem status definido pelo cliente", async () => {
      const cadastro = {
        contextoId: 2,
        titulo: "Assembleia",
        descricao: "Descrição",
        regraAprovacao: "MAIORIA_SIMPLES",
        percentualMinimo: null,
        quorumMinimo: 50,
        dataInicio: "2026-11-01T10:00:00",
        dataFim: "2026-11-02T10:00:00",
        pauta: "Pauta",
        participantesAptos: 15,
      } as const;
      const fetchMock = vi.fn().mockResolvedValue({ ok: true, json: vi.fn().mockResolvedValue(votacao) });
      vi.stubGlobal("fetch", fetchMock);

      await expect(cadastrarVotacao(cadastro)).resolves.toEqual(votacao);

      const [url, options] = fetchMock.mock.calls[0];
      expect(url).toMatch(/\/api\/votacoes$/);
      expect(options.method).toBe("POST");
      expect(JSON.parse(options.body)).toEqual(cadastro);
      expect(JSON.parse(options.body)).not.toHaveProperty("status");
    });
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