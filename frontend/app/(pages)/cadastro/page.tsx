"use client";

import { validarCadastro } from "@/app/actions/cadastro";
import { CadastroErrors, Usuario } from "@/entities/entities";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

export default function cadastro() {
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

    // Já poderemos converter para Number pois já verificamos
    const usuario: Usuario = {
      nome: data.nome.trim(),
      email: data.email.trim(),
      senha: data.senha.trim(),
    };

    try {
      const resposta = await fetch("http://localhost:8080/usuarios", {
        // Isso enviará para o nosso back, no formato JSON, os dados do nosso formulario (nome e email), quando o usuario clicar em cadastrar
        method: "POST", // Esse metodo diz que queremos inserir no banco. Existe, por exemplo o metodo GET (que fala que queremos pegar uma informação do banco)
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(usuario),
      });

      if (resposta.ok) {
        toast.success(`Usuário ${usuario.nome} cadastrado!`);
      } else {
        toast.error(`Erro ao cadastrar o usuário ${usuario.nome}.`);
      }
    } catch (err) {
      console.error("Erro inesperado:", err);
      toast.error("Erro ao enviar dados para o backend")
      // quando tivermos uma exception podemos pegar aq
    } finally {
      setCadastrar(false);
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
