"use client";
import Link from "next/link";
import CriarContexto from "@/components/criarContexto";
import EntrarContexto from "@/components/entrarContexto";
import Navbar from "@/components/navbar";
import SeletorContexto from "@/components/seletorContextos";
import {
  Aba,
  ContextoDTO,
  Membro,
  MembroChapa,
  UsuarioResponse,
  VotacaoResponse,
} from "@/entities/entities";
import {
  alterarPeso,
  buscarMembrosDoContextoParticipacao,
  buscarMensagens,
  buscarVotacoesPorContexto,
  deletarParticipacao,
  encerrarVotacao,
  enviarMensagem,
  getContextos,
  getIdUser,
  logoutUsuario,
  Mensagem,
} from "@/services/apiFacade";
import { useState, useEffect } from "react";
import { toast } from "sonner";
import { useRouter, useSearchParams } from "next/navigation";
import { MdBarChart, MdHowToVote, MdInfoOutline, MdSend } from "react-icons/md";
import { LuMessagesSquare } from "react-icons/lu";
import { HiTrash } from "react-icons/hi";
import { FiEdit3 } from "react-icons/fi";
import ModificarPeso from "@/components/modificarPeso";

export default function Home() {
  const router = useRouter();
  const [carregando, setCarregando] = useState(true);
  const [user, setUser] = useState<UsuarioResponse>();
  const [contextos, setContextos] = useState<ContextoDTO[]>([]);
  const [contextoAtual, setContextoAtual] = useState<ContextoDTO>();
  const [open, setOpen] = useState(false);
  const [abaSelecionada, setAbaSelecionada] = useState<Aba>("VOTACAO");
  const [criarContexto, setCriarContexto] = useState(false);
  const [entrarContexto, setEntrarContexto] = useState(false);
  const [logout, setLogout] = useState<boolean>(false);
  const [votacoes, setVotacoes] = useState<VotacaoResponse[]>([]);
  const [carregandoVotacoes, setCarregandoVotacoes] = useState(false);
  const [erroVotacoes, setErroVotacoes] = useState<string | null>(null);
  const [carregandoMembros, setCarregandoMembros] = useState(false);
  const [membros, setMembros] = useState<Membro[]>([]);
  const [erroMembros, setErroMembros] = useState<string | null>(null);
  const [mensagens, setMensagens] = useState<Mensagem[]>([]);
  const [textoMensagem, setTextoMensagem] = useState("");
  const [editarPeso, setEditarPeso] = useState(false);
  const [membro, setMembro] = useState<Membro>();
  const searchParams = useSearchParams();
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

        const idContextoURL = Number(searchParams.get("contextoId"));

        const contextoSelecionado = contextos.find(
          (contexto) => contexto.id === idContextoURL,
        );

        setContextoAtual(contextoSelecionado ?? contextos[0]);
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
    let cancelado = false;
    if (!contextoAtual) return;
    const contextoId = contextoAtual.id;

    async function carregarVotacoes() {
      setCarregandoVotacoes(true);
      setErroVotacoes(null);
      try {
        const resultado = await buscarVotacoesPorContexto(contextoId);
        if (!cancelado) setVotacoes(resultado);
      } catch (erro) {
        if (!cancelado) {
          setErroVotacoes(
            erro instanceof Error
              ? erro.message
              : "Não foi possível carregar as votações.",
          );
          setVotacoes([]);
        }
      } finally {
        if (!cancelado) setCarregandoVotacoes(false);
      }
    }

    void carregarVotacoes();
    return () => {
      cancelado = true;
    };
  }, [contextoAtual]);

  useEffect(() => {
    let cancelado = false;
    if (!contextoAtual) return;
    const contextoId = contextoAtual.id;

    async function carregarMembros() {
      setCarregandoMembros(true);
      setErroMembros(null);
      try {
        const resultado: Membro[] =
          await buscarMembrosDoContextoParticipacao(contextoId);
        console.log(resultado);
        const membrosOrdenados = [...resultado].sort((a, b) => {
          const tipoA = (a.papel ?? "").trim().toUpperCase();
          const tipoB = (b.papel ?? "").trim().toUpperCase();

          const aEhAdm = tipoA === "ADMINISTRADOR";
          const bEhAdm = tipoB === "ADMINISTRADOR";

          if (aEhAdm && !bEhAdm) return -1;
          if (!aEhAdm && bEhAdm) return 1;

          return tipoA.localeCompare(tipoB, "pt-BR");
        });

        setMembros(membrosOrdenados);
        if (!cancelado) setMembros(resultado);
      } catch (erro) {
        if (!cancelado) {
          setErroMembros(
            erro instanceof Error
              ? erro.message
              : "Não foi possível encontrar os membros do contexto.",
          );
          setMembros([]);
        }
      } finally {
        if (!cancelado) setCarregandoMembros(false);
      }
    }

    void carregarMembros();
    return () => {
      cancelado = true;
    };
  }, [contextoAtual]);

  useEffect(() => {
    async function carregarMensagens() {
      if (!contextoAtual || !user) return;

      try {
        const mensagens: Mensagem[] = await buscarMensagens(
          contextoAtual.id,
          user.id,
        );
        console.log(mensagens);
        setMensagens(mensagens);
      } catch (erro) {
        console.error("Erro ao carregar mensagens:", erro);
      }
    }

    carregarMensagens();
  }, [contextoAtual, user]);

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

  async function ExcluirParticipacao(idMembro: number) {
    if (!contextoAtual || !user) return;

    try {
      await deletarParticipacao(contextoAtual.id, user.id, idMembro);

      toast.success("Membro removido do contexto");

      setMembros((atuais) => atuais.filter((membro) => membro.id !== idMembro));
    } catch (erro) {
      console.error("Erro ao excluir participação:", erro);
      toast.error("Não foi possível remover o membro do contexto.");
    }
  }

  async function EnviarMensagem() {
    if (!contextoAtual || !user) return;

    try {
      const novaMensagem: Mensagem = await enviarMensagem(
        contextoAtual.id,
        user.id,
        textoMensagem,
      );

      setMensagens((mensagensAtuais) => [...mensagensAtuais, novaMensagem]); // Atualiza o array de mensagens com esta que acabou de ser feita
    } catch (erro) {
      console.error("Erro ao enviar mensagem:", erro);
    }
  }

  async function encerrar(votacaoId: number) {
    try {
      const atualizada = await encerrarVotacao(votacaoId);
      setVotacoes((atuais) =>
        atuais.map((v) => (v.id === atualizada.id ? atualizada : v)),
      );
    } catch (e) {
      setErroVotacoes(
        e instanceof Error ? e.message : "Não foi possível encerrar a votação.",
      );
    }
  }

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

  if (editarPeso && contextoAtual && membro) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-start gap-4 py-5">
        <ModificarPeso
          membro={membro}
          contextoAtual={contextoAtual.id}
          setModificarPeso={setEditarPeso}
        />{" "}
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
    if (!contextoAtual) return null;

    switch (abaSelecionada) {
      case "COMUNIDADE":
        return (
          <section className="rounded-2xl border-2 border-indigo-500 p-6 h-[74vh] justify-center items-center">
            <h2 className="text-xl font-semibold text-gray-900">Comunidade</h2>
            <p className="mt-2 text-gray-600">
              Acompanhe as conversas e novidades.
            </p>
            {/* Simulação de um chat */}
            <section className="h-[54vh] overflow-y-auto">
              {mensagens.length > 0 ? (
                mensagens.map((elem, index) =>
                  elem.usuarioId === user?.id ? (
                    <div key={index} className="flex flex-col items-end">
                      <div className="flex rounded-lg p-2 my-2 w-[50vh] bg-indigo-300 text-black">
                        {elem.texto}
                      </div>
                      <p className="text-sm text-gray-800">
                        {new Date(elem.dataEnvio).toLocaleString("pt-BR")}
                      </p>
                    </div>
                  ) : (
                    <div key={index} className="flex flex-col items-start">
                      <div
                        key={index}
                        className="flex rounded-lg p-2  my-2 w-[50vh] bg-indigo-200 text-black"
                      >
                        {elem.texto}
                      </div>
                      <p className="text-sm text-gray-800">
                        {new Date(elem.dataEnvio).toLocaleString("pt-BR")}
                      </p>
                    </div>
                  ),
                )
              ) : (
                <div className="flex flex-col items-center justify-center gap-3 py-12 mx-[14vw]">
                  <div className="flex items-center justify-center w-14 h-14 rounded-full bg-indigo-100">
                    <LuMessagesSquare size={30} className="text-indigo-500" />
                  </div>

                  <p className="text-gray-700 text-center text-lg font-medium">
                    Este contexto ainda não tem nenhuma mensagem.
                  </p>

                  <p className="text-gray-500 text-sm text-center">
                    Quando uma mensagem for enviada, ela aparecerá aqui.
                  </p>
                </div>
              )}
            </section>
            <div className="flex justify-center gap-2">
              <input
                className="bg-white rounded-2xl h-[5vh] w-[80vh] justify-end items-end text-black border-2 border-indigo-500 hover:border-indigo-500 mt-4 p-3"
                placeholder="Digite a mensagem..."
                type="text"
                value={textoMensagem}
                onChange={(e) => setTextoMensagem(e.target.value)}
              ></input>
              <button
                className="flex justify-center bg-indigo-500 w-[7vh] h-[5vh] mt-4 p-3 rounded-xl"
                onClick={() => {
                  EnviarMensagem();
                  setTextoMensagem("");
                }}
              >
                <MdSend color="white" size={24} />
              </button>
            </div>
          </section>
        );

      case "VOTACAO":
        return (
          <section className="rounded-2xl border-2 border-indigo-500 p-6 h-[74vh] overflow-y-auto">
            <div className="flex flex-row justify-between">
              <div>
                <h2 className="text-xl font-semibold">Votações</h2>
                <p className="mt-2 text-gray-600">
                  Consulte todas as votações.
                </p>
              </div>
              {contextoAtual.papel == "ADMINISTRADOR" && (
                <div className="flex justify-center items-center">
                  <Link
                    href={`/contextos/${contextoAtual.id}/votacoes/nova`}
                    className="inline-block rounded-2xl bg-indigo-300 hover:bg-indigo-300/85  p-3 text-center itens-center text-gray-800"
                  >
                    Criar nova votação
                  </Link>
                </div>
              )}
            </div>
            <section className="flex flex-col gap-3 py-3">
              {carregandoVotacoes && (
                <p role="status">Carregando votações...</p>
              )}
              {!carregandoVotacoes && erroVotacoes && (
                <p className="text-red-700">{erroVotacoes}</p>
              )}
              {!carregandoVotacoes &&
                !erroVotacoes &&
                votacoes.length === 0 && (
                  <div className="flex flex-col items-center justify-center gap-3 py-12 mx-[19vw]">
                    <div className="flex items-center justify-center w-14 h-14 rounded-full bg-indigo-100">
                      <MdHowToVote size={30} className="text-indigo-500" />
                    </div>

                    <p className="text-gray-700 text-center text-lg font-medium">
                      Este contexto ainda não tem votações.
                    </p>

                    <p className="text-gray-500 text-sm text-center">
                      Quando uma votação for criada, ela aparecerá aqui.
                    </p>
                  </div>
                )}
              {!carregandoVotacoes &&
                !erroVotacoes &&
                votacoes.map((votacao) => (
                  <section
                    key={votacao.id}
                    className="flex flex-col gap-3 rounded-2xl bg-indigo-100 p-4 transition-all hover:bg-indigo-200/60"
                  >
                    <div className="flex items-center justify-between gap-4">
                      <h3 className="font-semibold text-indigo-950 line-clamp-1">
                        {votacao.titulo}
                      </h3>
                      <div className="flex flex-row gap-2">
                        <span
                          className={`flex shrink-0 items-center gap-2 rounded-full border-2 px-3 py-1 text-xs font-medium ${
                            votacao.tipo == "DELIBERACAO"
                              ? "bg-violet-100 border-violet-500 text-violet-900"
                              : "bg-teal-100 border-teal-500 text-teal-800"
                          }`}
                        >
                          <span>
                            {votacao.tipo == "ELEICAO"
                              ? "ELEIÇÃO"
                              : "DELIBERAÇÃO"}
                          </span>
                        </span>

                        <span className="flex shrink-0 items-center gap-2 rounded-full border-2 border-indigo-500 px-3 py-1 text-xs font-medium">
                          <span>{votacao.status}</span>
                          <span
                            className={`h-2 w-2 rounded-full ${votacao.status === "ENCERRADA" ? "bg-red-500" : "bg-green-500"}`}
                          />
                        </span>
                      </div>
                    </div>
                    <p className="line-clamp-2 text-sm text-indigo-900/80">
                      {votacao.descricao}
                    </p>
                    <section className="flex flex-wrap items-center gap-3 mt-4">
                      <Link
                        href={`contextos/${contextoAtual.id}/votacoes/${votacao.id}`}
                        className="flex items-center gap-2 px-4 py-2 rounded-lg bg-white text-indigo-600 border border-indigo-300 font-medium text-sm transition-all duration-20 hover:bg-indigo-50 hover:border-indigo-400  hover:shadow-sm"
                      >
                        <MdInfoOutline size={19} />
                        Detalhes
                      </Link>

                      <Link
                        href={`/contextos/${contextoAtual.id}/votacoes/${votacao.id}#votar`}
                        className="flex items-center gap-2 px-4 py-2 rounded-lg bg-white text-indigo-600 border border-indigo-300 font-medium text-sm transition-all duration-20 hover:bg-indigo-50 hover:border-indigo-400  hover:shadow-sm"
                      >
                        <MdHowToVote size={19} />
                        Votar
                      </Link>

                      <Link
                        href={`/contextos/${contextoAtual.id}/votacoes/${votacao.id}#apuracao-titulo`}
                        className="flex items-center gap-2 px-4 py-2 rounded-lg bg-white text-indigo-600 border border-indigo-300 font-medium text-sm transition-all duration-20 hover:bg-indigo-50 hover:border-indigo-400  hover:shadow-sm"
                      >
                        <MdBarChart size={19} />
                        Resultados
                      </Link>
                      {contextoAtual.papel === "ADMINISTRADOR" &&
                        votacao.status === "ABERTA" && (
                          <button
                            type="button"
                            onClick={() => encerrar(votacao.id)}
                            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-white text-red-700 border border-red-300 font-medium text-sm hover:bg-red-50"
                          >
                            Encerrar
                          </button>
                        )}
                    </section>
                    <div className="flex items-center justify-between border-t border-indigo-300/90 pt-2 text-xs text-indigo-700">
                      <span>
                        Início:
                        {votacao.dataInicio
                          ? new Date(votacao.dataInicio).toLocaleString()
                          : "Não definido"}
                      </span>
                      <span>
                        Fim:{" "}
                        {votacao.dataFim
                          ? new Date(votacao.dataFim).toLocaleString()
                          : "Não definido"}
                      </span>
                    </div>
                  </section>
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
              {membros.length > 0 ? (
                membros.map((membro) => (
                  <div
                    key={membro.id}
                    className="flex items-center gap-4 bg-indigo-50 border border-indigo-200 rounded-2xl px-4 py-3 transition-all hover:shadow-sm"
                  >
                    <div className="flex items-center justify-center w-11 h-11 rounded-full bg-indigo-200 text-indigo-800 font-semibold text-sm shrink-0">
                      {membro.nome.charAt(0).toUpperCase()}
                    </div>

                    <div className="min-w-0 flex-1">
                      <p className="text-base font-semibold text-gray-800 truncate">
                        {membro.nome}
                      </p>
                      <p className="text-sm text-gray-500 truncate">
                        {membro.email}
                      </p>
                    </div>
                    <div className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-sm font-medium text-teal-500 hover:bg-teal-200/70 bg-teal-100 transition-colors shrink-0">
                      {membro.papel}
                    </div>
                    {/* Remover membro e modificar peso - somente administrador */}
                    {contextoAtual.papel === "ADMINISTRADOR" && (
                      <div className="flex flex-row gap-3">
                        {contextoAtual.tipo == "CONDOMINIO" && (
                          <button
                            type="button"
                            onClick={() => (
                              setEditarPeso(true),
                              setMembro(membro)
                            )}
                            className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-sm font-medium text-blue-600 hover:bg-blue-200/70 bg-blue-100 transition-colors shrink-0"
                          >
                            <FiEdit3 className="text-blue-600" size={18} />
                            Modificar peso
                          </button>
                        )}

                        {user && membro.id != user.id && (
                          <button
                            type="button"
                            onClick={() => {
                              ExcluirParticipacao(membro.id);
                            }}
                            className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-sm font-medium text-red-600 bg-red-200/70 hover:bg-red-50 transition-colors shrink-0"
                          >
                            <HiTrash color="red" size={18} />
                            Remover
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                ))
              ) : (
                <p className="text-sm text-gray-500 text-center py-6">
                  Nenhum membro encontrado neste contexto.
                </p>
              )}
            </section>
          </section>
        );
    }
  }
  // front basico só para teste
  return (
    <main className="min-h-screen bg-gray-50 text-gray-800">
      <header className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-6 py-6">
        <div>
          <div className="flex flex-col gap-1 text-xl font-semibold mr-3">
            <p>Olá, {user?.nome}!</p>
            <p>Você está no contexto {contextoAtual?.nome}</p>
          </div>
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
              setAbaSelecionada("VOTACAO");
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
