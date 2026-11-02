"use client";
import Navbar from "@/components/navbar";
import SeletorContexto from "@/components/seletorContextos";
import { Aba, ContextoDTO, Usuario } from "@/entities/entities";
import { getContextos } from "@/services/apiFacade";
import { useState, useEffect } from "react";

export default function Home() {
  const [carregando, setCarregando] = useState(true);
  const [user, setUser] = useState<Usuario>();
  const [contextos, setContextos] = useState<ContextoDTO[]>([]);
  const [contextoAtual, setContextoAtual] = useState<ContextoDTO>();
  const [open, setOpen] = useState(false);
  const [abaSelecionada, setAbaSelecionada] = useState<Aba>("comunidade");

  useEffect(() => {
    async function carregarDados() {
      setCarregando(true);

      try {
        // Fazer get do user
        const resposta: Response = await getContextos();

        if (!resposta.ok) {
          const erro = await resposta.text();
          console.error("Erro ao buscar contextos:", erro);
          return;
        }

        const contextos: ContextoDTO[] = await resposta.json();
        setContextoAtual(contextos[0]);

        console.log("Contextos recebidos:", contextos);
        setContextos(contextos);
      } catch (erro) {
        console.error("Erro na requisição:", erro);
      } finally {
        setCarregando(false);
      }
    }

    carregarDados();
  }, []);

  if (carregando) {
    return (
      <main className="flex min-h-screen items-center justify-center text-[#376DF1]">
        Carregando...
      </main>
    );
  }

  if (!contextoAtual) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-6">
        <header className="mx-auto flex max-w-8xl items-center justify-between gap-4 px-6 py-6">
          <h1 className="text-2xl font-bold text-indigo-500">
            Olá, {user?.nome}!
          </h1>
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
              window.location.href = "/";
            }}
            className="rounded-xl bg-indigo-500 px-5 py-3 text-white hover:bg-blue-700"
          >
            Criar novo contexto
          </button>

          <button
            onClick={() => {
              window.location.href = "/";
            }}
            className="rounded-xl bg-indigo-500 px-5 py-3 text-white hover:bg-blue-700"
          >
            Entrar em um contexto existente
          </button>
        </div>
      </main>
    );
  }

  function renderizarConteudo() {
    switch (abaSelecionada) {
      case "comunidade":
        return (
          <section className="rounded-2xl bg-indigo-100 p-6 min-h-screen">
            <h2 className="text-xl font-semibold text-gray-900">Comunidade</h2>
            <p className="mt-2 text-gray-600">
              Acompanhe as conversas e novidades.
            </p>
          </section>
        );

      case "votacao":
        return (
          <section className="rounded-2xl bg-indigo-100 p-6 min-h-screen">
            <h2 className="text-xl font-semibold">Votações</h2>
            <p className="mt-2 text-gray-600">
              Consulte as votações disponíveis.
            </p>

            {/* Exibir as votações disponiveis: Deixei um exemplo para visualizarmos apenas */}
            <section className="flex flex-col gap-3 py-3">
              <div className="bg-white rounded-2xl h-24 px-4 py-3">
                Votação 1
              </div>
              <div className="bg-white rounded-2xl h-24 px-4 py-3">
                Votação 2
              </div>
              <div className="bg-white rounded-2xl h-24 px-4 py-3">
                Votação 3
              </div>
              <div className="bg-white rounded-2xl h-24 px-4 py-3">
                Votação 4
              </div>
            </section>
          </section>
        );

      case "propostas":
        return (
          <section className="rounded-2xl bg-indigo-100 p-6 min-h-screen shadow-sm">
            <h2 className="text-xl font-semibold">
              {contextoAtual!.tipo === "ASSEMBLEIA" ? "Chapas" : "Propostas"}
            </h2>
            <p className="mt-2 text-gray-600">
              Consulte as informações deste contexto.
            </p>
          </section>
        );

      case "configurar-votacao":
        return (
          <section className="rounded-2xl bg-white p-6 shadow-sm">
            <h2 className="text-xl font-semibold">Configurar votação</h2>
            <p className="mt-2 text-gray-600">
              Crie e gerencie as votações deste contexto.
            </p>
          </section>
        );
    }
  }
  // front basico só para teste
  return (
    <main className="min-h-screen bg-gray-50 text-gray-900">
      <header className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-6 py-6">
        <div>
          <h1 className="text-2xl font-bold">Olá, {user?.nome}!</h1>
          <p className="mt-1 text-sm text-gray-500">
            Você está no contexto {contextoAtual?.nome}.
          </p>
        </div>
        <div className="flex flex-row gap-3">
          <SeletorContexto
            contextos={contextos}
            contextoAtual={contextoAtual}
            open={open}
            setOpen={setOpen}
            onSelecionar={(contexto) => {
              setContextoAtual(contexto);
              setAbaSelecionada("comunidade");
            }}
            onCriar={() => {
              window.location.href = "/contextos/criar";
            }}
            onEntrar={() => {
              window.location.href = "/contextos/entrar";
            }}
          />
          <button className="bg-indigo-500 rounded-xl text-white w-24 h-12">
            Sair
          </button>
        </div>
      </header>

      <div className="mx-auto max-w-6xl px-6">
        <Navbar
          abaSelecionada={abaSelecionada}
          setAbaSelecionada={setAbaSelecionada}
          ehAdmin={false}
          tipoContexto={contextoAtual.tipo}
        />

        <div className="py-8">{renderizarConteudo()}</div>
      </div>
    </main>
  );
}
