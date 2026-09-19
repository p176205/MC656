"use client";

import { CadastroErrors, Usuario, UsuarioForm } from "@/entities/entities";
import { useEffect, useState } from "react";
import { SubmitHandler, useForm } from "react-hook-form";
import { toast } from "sonner";

export default function cadastro() {
  // Aqui ficam function, estados (usamos o useState para isso), efeitos colaterais (usamos useEffect para isso)
  const [cadastrar, setCadastrar] = useState<boolean>(false);
  const [errors, setErrors] = useState<CadastroErrors>({
    nome: { message: "", status: false },
    email: { message: "", status: false },
    senha: { message: "", status: false },
  });

  function validarCadastro(
    nome: string,
    email: string,
    senha: string,
  ): CadastroErrors {
    const errors: CadastroErrors = {
      nome: { message: "", status: false },
      email: { message: "", status: false },
      senha: { message: "", status: false },
    }; // cria um objeto do tipo CadastroErrors vazio
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // Validação do nome
    if (!nome.trim()) {
      errors.nome = {
        message: "O nome é obrigatório",
        status: true,
      };
    }
    // Validação do e-mail
    if (!email.trim()) {
      // .trim() retira espaço inicial e final da string
      errors.email = {
        message: "O e-mail é obrigatório",
        status: true,
      };
    } else if (!emailRegex.test(email)) {
      errors.email = {
        message: "O e-mail informado é inválido",
        status: true,
      };
    }

    // Validação da senha
    if (!senha.trim()) {
      errors.senha = {
        message: "A senha é obrigatória",
        status: true,
      };
    } else if (senha.length < 6) {
      errors.senha = {
        message: "A senha deve conter pelo menos 6 números",
        status: true,
      };
    } else if (!Number(senha.trim())) {
      errors.senha = {
        message: "A senha deve conter apenas números",
        status: true,
      };
    }
    return errors;
  }

  const { register, handleSubmit} = useForm<UsuarioForm>({
    defaultValues: {
      nome: "",
      email: "",
      senha: "",
    },
    mode: "onChange", // Atualizará valores conforme cada caractere digitado, o que permite que mensagens de erro nao sejam exibidas só ao enviar o form
  });

  const onSubmit = async (data: UsuarioForm) => {
    console.log("Enviando formulario", data);
    setCadastrar(true);

    const formularioValido: CadastroErrors = validarCadastro(
      data.nome,
      data.email,
      data.senha,
    );

    const temErros = Object.entries(formularioValido).some(
      ([chave, valor]) => valor.status === true,
    );
    if (temErros) {
      setErrors(formularioValido);
      return;
    }

    try {
      const resposta = await fetch("http://localhost:8080/usuarios", {
        // Isso enviará para o nosso back, no formato JSON, os dados do nosso formulario (nome e email), quando o usuario clicar em cadastrar
        method: "POST", // Esse metodo diz que queremos inserir no banco. Existe, por exemplo o metodo GET (que fala que queremos pegar uma informação do banco)
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nome: data.nome.trim(),
          email: data.email.trim(),
          senha: Number(data.senha.trim()),
        }),
      });

      if (resposta.ok) {
        toast(`Usuário ${data.nome} cadastrado!`);
      } else {
        toast(`Erro ao cadastrar o usuário ${data.nome}.`);
      }
    } catch (err) {
      console.error("Erro inesperado:", err);
      // quando tivermos uma exception podemos pegar aq
    } finally {
      //
    }
  };

  // No return fica o "html e o css", soó que em um formato mais compacto onde misturamos html e css (se não seriam dois arquivos separados). onde fazemos o visual da pagina
  return (
    <main className="min-h-screen flex flex-col items-center justify-center gap-8 p-5 text-black">
      <h1 className="text-2xl font-bold">Cadastre um usuário</h1>

      <form
        className="flex w-full max-w-md flex-col gap-5 rounded-lg border p-6 shadow-md"
        onSubmit={handleSubmit(onSubmit)}
      >
        <div className="flex flex-col gap-2">
          <label className="font-medium">Nome</label>

          <input
            className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
            type="text"
            {...register("nome")}
            onChange={() =>
              setErrors((prev) => ({
                ...prev,
                nome: {
                  message: "",
                  status: false,
                },
              }))
            }
          />
          {/* Se possuir erro exibiremos uma mensagem abaixo do input, div className="min-h-[24px] (define um espaço para a mensagem de erro e impede que o conteudo "pule" ao exibir a mensagem*/}
          <div className="min-h-[15]px mt-1">
            {errors.nome.status === true && (
              <p className="text-red-600 text-base">{errors.nome.message}</p>
            )}
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <label className="font-medium">E-mail</label>

          <input
            className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
            type="email"
            {...register("email")}
            onChange={() =>
              setErrors((prev) => ({
                ...prev,
                email: {
                  message: "",
                  status: false,
                },
              }))
            }
          />
          {/* Se possuir erro exibiremos uma mensagem abaixo do input, div className="min-h-[24px] (define um espaço para a mensagem de erro e impede que o conteudo "pule" ao exibir a mensagem*/}
          <div className="min-h-[15]px mt-1">
            {errors.email.status === true && (
              <p className="text-red-600 text-base">{errors.email.message}</p>
            )}
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <label className="font-medium">Senha</label>

          <input
            className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
            type="number"
            {...register("senha")}
            onChange={() =>
              setErrors((prev) => ({
                ...prev,
                senha: {
                  message: "",
                  status: false,
                },
              }))
            }
          />
          {/* Se possuir erro exibiremos uma mensagem abaixo do input, div className="min-h-[24px] (define um espaço para a mensagem de erro e impede que o conteudo "pule" ao exibir a mensagem*/}
          <div className="min-h-[15]px mt-1">
            {errors.senha.status && (
              <p className="text-red-600 text-base">{errors.senha.message}</p>
            )}
          </div>
        </div>

        <button
          className="rounded-md bg-purple-600 p-2 font-medium text-white hover:bg-purple-700 disabled:opacity-60"
          type="submit"
          disabled={cadastrar}
        >
          {cadastrar ? "Cadastrando..." : "Cadastrar"}
        </button>
      </form>
    </main>
  );
}
