"use client";
import CriarContexto from "@/components/criarContexto";
import EntrarContexto from "@/components/entrarContexto";
import Navbar from "@/components/navbar";
import SeletorContexto from "@/components/seletorContextos";
import {
  Aba,
  ContextoDTO,
  UsuarioResponse,
  Votacao,
} from "@/entities/entities";
import { getContextos, getIdUser, logoutUsuario } from "@/services/apiFacade";
import { useState, useEffect } from "react";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

export default function Home() {
  const router = useRouter();
  const [carregando, setCarregando] = useState(true);
  const [user, setUser] = useState<UsuarioResponse>();
  const [contextos, setContextos] = useState<ContextoDTO[]>([]);
  const [contextoAtual, setContextoAtual] = useState<ContextoDTO>();
  const [open, setOpen] = useState(false);
  const [abaSelecionada, setAbaSelecionada] = useState<Aba>("COMUNIDADE");
  const [criarContexto, setCriarContexto] = useState(false);
  const [entrarContexto, setEntrarContexto] = useState(false);
  const [logout, setLogout] = useState<boolean>(false);
  const [votacoes, setVotacoes] = useState<Votacao[]>([
    {
      titulo: "Votacao 1",
      descricao: "Votacao descricao",
      //contexto: ,
      tipo: "DELIBERACAO",
      regra_aprovacao: "MAIORIA_SIMPLES",
      quorum_minimo: 100,
      status: "ENCERRADA",
      data_inicio: "11-02-2026",
      data_fim: "11-22-2026",
    },
    {
      titulo: "Votacao 2",
      descricao: "Votacao descricao 2",
      //contexto: ,
      tipo: "DELIBERACAO",
      regra_aprovacao: "MAIORIA_SIMPLES",
      quorum_minimo: 100,
      status: "ABERTA",
      data_inicio: "11-02-2026",
      data_fim: "11-22-2026",
    },
  ]);
  useEffect(() => {
    async function carregarDados() {
      setCarregando(true);

      try {
        // 1. Obtém o ID do usuário autenticado
        const usuario: UsuarioResponse = await getIdUser();

        console.log("ID do usuário:", usuario);
        setUser(usuario);
        // 2. Busca os contextos associados ao usuário
        const resposta = await getContextos(usuario.id);

        if (!resposta.ok) {
          const erro = await resposta.text();
          throw new Error(`Erro ao buscar contextos: ${erro}`);
        }

        // 3. Converte a resposta da API para uma lista de contextos
        const contextos: ContextoDTO[] = await resposta.json();

        console.log("Contextos recebidos:", contextos);
        setContextos(contextos);

        if (contextos.length > 0) {
          setContextoAtual(contextos[0]);
        }
      } catch (erro) {
        if (erro instanceof Error) {
          console.error("Erro ao carregar dados:", erro.message);
        }
      } finally {
        setCarregando(false);
      }
    }

    carregarDados();
  }, []);

  useEffect(() => {
    if (!user && !carregando) {
      toast.warning(
        "Você precisa estar logado para criar ou entrar em um contexto.",
      );
    }
  }, [user, carregando]); // Só dispara se contextoAtual ou user mudarem

  useEffect(() => {
    if (!logout) return;

    async function executarLogout() {
      try {
        const resultado = await logoutUsuario();

        if (resultado.ok) {
          router.push("/");
        } else {
          console.error("Erro ao fazer logout:", resultado.message);
        }
      } catch (err) {
        console.error("Erro", err);
      } finally {
        setLogout(false);
      }
    }

    executarLogout();
  }, [logout, router]);

  if (carregando) {
    return (
      <main className="flex min-h-screen items-center justify-center text-[#376DF1]">
        Carregando...
      </main>
    );
  }

  if (criarContexto && user) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-start gap-4 py-5">
        <CriarContexto user={user} setCriarContexto={setCriarContexto} />
      </main>
    );
  }

  if (entrarContexto && user) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-start gap-4 py-5">
        <EntrarContexto user={user} setEntrarContexto={setEntrarContexto} />
      </main>
    );
  }

  if (!contextoAtual) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-start gap-4 py-5">
        <header className="flex w-full max-w-8xl items-center justify-between gap-8 px-12 pb-6 pt-0">
          <h1 className="text-2xl font-bold text-indigo-500">
            Olá, {user?.nome}!
          </h1>
          <button
            className="bg-white border-2 border-indigo-500 rounded-xl text-gray-700 w-24 h-12"
            onClick={() => {
              setLogout(true);
            }}
            disabled={!user}
          >
            Sair
          </button>
        </header>
        <h1 className="text-xl font-semibold text-gray-800">
          Você ainda não está em nenhum contexto
        </h1>
        <img src="image.png" alt="image" width={600} />
        <p className="text-gray-500">
          Crie um contexto ou entre em um existente para continuar.
        </p>
        <div className="flex flex-row gap-4">
          <button
            onClick={() => {
              setCriarContexto(true);
            }}
            className={`rounded-xl px-5 py-3 text-white ${!user ? `bg-gray-400` : `bg-indigo-500  hover:bg-indigo-600`}`}
            disabled={!user}
          >
            Criar novo contexto
          </button>

          <button
            onClick={() => {
              setEntrarContexto(true);
            }}
            className={`rounded-xl px-5 py-3 text-white ${!user ? `bg-gray-400` : `bg-indigo-500  hover:bg-indigo-600`}`}
            disabled={!user}
          >
            Entrar em um contexto existente
          </button>
        </div>
      </main>
    );
  }

  function renderizarConteudo() {
    switch (abaSelecionada) {
      case "COMUNIDADE":
        return (
          <section className="rounded-2xl border-2 border-indigo-500 p-6 h-[74vh] overflow-y-auto">
            <h2 className="text-xl font-semibold text-gray-900">{}</h2>
            <p className="mt-2 text-gray-600">
              Acompanhe as conversas e novidades.
            </p>
          </section>
        );

      case "VOTACAO":
        return (
          <section className="rounded-2xl border-2 border-indigo-500 p-6 h-[74vh] overflow-y-auto">
            <h2 className="text-xl font-semibold">Votações</h2>
            <p className="mt-2 text-gray-600">
              Consulte as votações disponíveis.
            </p>

            {/* Exibir as votações disponiveis: Deixei um exemplo para visualizarmos apenas */}
            <section className="flex flex-col gap-3 py-3">
              {votacoes.map((votacao) => (
                <div className="flex flex-col gap-3 rounded-2xl bg-indigo-100 p-4 transition-all hover:bg-indigo-200/60">
                  <div className="flex items-center justify-between gap-4">
                    <h3 className="font-semibold text-indigo-950 line-clamp-1">
                      {votacao.titulo}
                    </h3>
                    <span
                      className={`flex flex-row items-center gap-2 shrink-0 rounded-full border-2 px-3 py-1 text-xs font-medium border-indigo-500`}
                    >
                      <span>{votacao.status}</span>
                      <span
                        className={`w-2 h-2 rounded-full ${votacao.status === "ENCERRADA" ? "bg-red-500" : "bg-green-500"}`}
                      ></span>
                    </span>
                  </div>
                  <p className="text-sm text-indigo-900/80 line-clamp-2">
                    {votacao.descricao}
                  </p>
                  <div className="flex items-center justify-between border-t border-indigo-300/90 pt-2 text-xs text-indigo-700">
                    <span>
                      Início:{" "}
                      {new Date(votacao.data_inicio).toLocaleDateString()}
                    </span>
                    <span>
                      Fim: {new Date(votacao.data_fim).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              ))}
            </section>
          </section>
        );

      case "MEMBROS":
        return (
          <section className="rounded-2xl border-2 border-indigo-500 p-6 h-[74vh] overflow-y-auto">
            <h2 className="text-xl font-semibold text-gray-900">Membros</h2>
            <p className="mt-2 text-gray-600">
              Lista completa de todos os membros.
            </p>

            <section className="flex flex-col gap-3 py-3">
              <div className="bg-indigo-100 rounded-2xl h-24 px-4 py-3">
                Membro 1
              </div>
              <div className="bg-indigo-100 rounded-2xl h-24 px-4 py-3">
                Membro 2
              </div>
              <div className="bg-indigo-100 rounded-2xl h-24 px-4 py-3">
                Membro 3
              </div>
              <div className="bg-indigo-100 rounded-2xl h-24 px-4 py-3">
                Membro 4
              </div>
              <div className="bg-indigo-100 rounded-2xl h-24 px-4 py-3">
                Membro 4
              </div>
              <div className="bg-indigo-100 rounded-2xl h-24 px-4 py-3">
                Membro 4
              </div>{" "}
              <div className="bg-indigo-100 rounded-2xl h-24 px-4 py-3">
                Membro 4
              </div>{" "}
            </section>
          </section>
        );
    }
  }
  // front basico só para teste
  return (
    <main className="min-h-screen bg-gray-50 text-gray-900">
      <header className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-6 py-6">
        <div>
          <h1 className="text-2xl font-bold">
            Olá, {user?.nome}! Você está no contexto {contextoAtual?.nome}
          </h1>
          <div
            className="flex flex-row gap-1"
            hidden={contextoAtual.papel != "ADMINISTRADOR"}
          >
            <p className="mt-1 text-lg text-gray-700">Código do contexto: </p>
            <p className="mt-1 text-lg text-indigo-500 ">
              {contextoAtual.contexto.codigo}
            </p>
          </div>
        </div>
        <div className="flex flex-row gap-3">
          <div className="flex flex-col gap-1 justify-center items-center border-b-indigo-400 border-b-2 p-1 text-sm">
            {contextoAtual.papel}
          </div>
          <SeletorContexto
            contextos={contextos}
            contextoAtual={contextoAtual}
            open={open}
            setOpen={setOpen}
            onSelecionar={(contexto) => {
              setContextoAtual(contexto);
              setAbaSelecionada("COMUNIDADE");
            }}
            onCriar={() => {
              setCriarContexto(true);
            }}
            onEntrar={() => {
              setEntrarContexto(true);
            }}
          />
          <button
            className="bg-white border-2 border-indigo-500 rounded-xl text-gray-700 w-24 h-12"
            onClick={() => {
              setLogout(true);
            }}
          >
            Sair
          </button>
        </div>
      </header>

      <div className="mx-auto max-w-6xl px-6">
        <Navbar
          abaSelecionada={abaSelecionada}
          setAbaSelecionada={setAbaSelecionada}
        />

        <div className="py-8">{renderizarConteudo()}</div>
      </div>
    </main>
  );
}
