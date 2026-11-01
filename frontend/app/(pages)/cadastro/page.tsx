"use client";

import { validarCadastro } from "@/app/actions/cadastro";
import { CadastroErrors, Usuario } from "@/entities/entities";
import { cadastrarUsuario, ResultadoApi } from "@/services/apiFacade";
import Link from "next/link";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

export default function Cadastro() {
  // Aqui ficam function, estados (usamos o useState para isso), efeitos colaterais (usamos useEffect para isso)

  const [cadastrar, setCadastrar] = useState<boolean>(false);
  const [errors, setErrors] = useState<CadastroErrors>({
    nome: { message: "", status: false },
    email: { message: "", status: false },
    senha: { message: "", status: false },
  });

  const { register, handleSubmit } = useForm<Usuario>({
    defaultValues: {
      nome: "",
      email: "",
      senha: "",
    },
    mode: "onChange", // Atualizará valores conforme cada caractere digitado
  });

  const onSubmit = async (data: Usuario) => {
    console.log("Enviando formulario", data);
    setCadastrar(true);

    const formularioValido: CadastroErrors = validarCadastro(data);

    const temErros = Object.entries(formularioValido).some(
      ([chave, valor]) => valor.status === true,
    );

    if (temErros) {
      setErrors(formularioValido);
      setCadastrar(false);
      return;
    }

    // Retirando espaços em branco no inicio e fim da string
    const usuario: Usuario = {
      nome: data.nome.trim(),
      email: data.email.trim(),
      senha: data.senha.trim(),
    };

    try {
      const resposta: ResultadoApi = await cadastrarUsuario(usuario);

      if (resposta.ok) {
        toast.success(`Usuário ${usuario.nome} cadastrado!`);
      } else {
        console.log("res:", resposta.message);
        toast.error(`Erro ao cadastrar o usuário ${usuario.nome}.`);
      }
    } catch (err) {
      console.error("Erro inesperado:", err);
    } finally {
      setCadastrar(false);
    }
  };

  // No return fica o "html e o css", soó que em um formato mais compacto onde misturamos html e css (se não seriam dois arquivos separados). onde fazemos o visual da pagina
  return (
    <main className="min-h-screen flex items-center justify-center p-4 bg-slate-100 text-slate-800">
      <div className="flex flex-col items-center justify-between w-full max-w-xl h-[90vh] min-h-[600px] bg-[#E6E8FD] rounded-3xl border-r-8 border-r-indigo-500 shadow-2xl px-8 py-22 transition-all">
        {/* Cabeçalho */}
        <div className="text-center pt-4">
          <h1 className="text-3xl font-bold tracking-tight text-indigo-950">
            CADASTRO
          </h1>
          <p className="text-sm text-slate-600 mt-1">
            Preencha os campos para criar sua conta
          </p>
        </div>

        {/* Formulário */}
        <form
          className="flex w-full flex-col gap-4 my-auto px-2"
          onSubmit={handleSubmit(onSubmit)}
        >
          <div className="flex flex-col gap-1.5">
            <label className="font-semibold text-sm text-indigo-900">
              Nome
            </label>
            <input
              className="rounded-xl border border-indigo-200 bg-white/80 backdrop-blur-sm p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
              type="text"
              placeholder="Digite seu nome completo"
              {...register("nome")}
              onChange={(event) => {
                register("nome").onChange(event);

                setErrors((prev) => ({
                  ...prev,
                  nome: {
                    message: "",
                    status: false,
                  },
                }));
              }}
            />
            {/* Se possuir erro exibiremos uma mensagem abaixo do input, div className="min-h-[24px] (define um espaço para a mensagem de erro e impede que o conteudo "pule" ao exibir a mensagem */}
            <div className="min-h-[20px] mt-1">
              {errors.nome.status === true && (
                <p className="text-red-500 text-xs font-medium pl-1">
                  {errors.nome.message}
                </p>
              )}
            </div>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="font-semibold text-sm text-indigo-900">
              E-mail
            </label>
            <input
              className="rounded-xl border border-indigo-200 bg-white/80 backdrop-blur-sm p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
              type="email"
              placeholder="exemplo@email.com"
              {...register("email")}
              onChange={(event) => {
                register("email").onChange(event);

                setErrors((prev) => ({
                  ...prev,
                  email: {
                    message: "",
                    status: false,
                  },
                }));
              }}
            />
            {/* Se possuir erro exibiremos uma mensagem abaixo do input, div className="min-h-[24px] (define um espaço para a mensagem de erro e impede que o conteudo "pule" ao exibir a mensagem */}
            <div className="min-h-[20px] mt-1">
              {errors.email.status === true && (
                <p className="text-red-500 text-xs font-medium pl-1">
                  {errors.email.message}
                </p>
              )}
            </div>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="font-semibold text-sm text-indigo-900">
              Senha
            </label>
            <input
              className="rounded-xl border border-indigo-200 bg-white/80 backdrop-blur-sm p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
              type="password"
              {...register("senha")}
              onChange={(event) => {
                register("senha").onChange(event);

                setErrors((prev) => ({
                  ...prev,
                  senha: {
                    message: "",
                    status: false,
                  },
                }));
              }}
            />
            {/* Se possuir erro exibiremos uma mensagem abaixo do input, div className="min-h-[24px] (define um espaço para a mensagem de erro e impede que o conteudo "pule" ao exibir a mensagem */}
            <div className="min-h-[20px] mt-1">
              {errors.senha.status && (
                <p className="text-red-500 text-xs font-medium pl-1">
                  {errors.senha.message}
                </p>
              )}
            </div>
          </div>

          <button
            className="mt-4 rounded-xl bg-indigo-500 p-3.5 font-semibold text-white shadow-lg shadow-indigo-500/30 transition-all duration-200 hover:bg-indigo-700 hover:shadow-indigo-500/40 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
            type="submit"
            disabled={cadastrar}
          >
            {cadastrar ? "Cadastrando..." : "Cadastrar"}
          </button>
          <Link href="/" className="underline hover:text-indigo-500 justify-center text-center pt-3">
                Voltar para o Login
            </Link>
        </form>
      </div>
    </main>
  );
}
