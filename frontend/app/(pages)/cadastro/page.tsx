"use client";

import { CadastroErrors, Usuario, UsuarioForm } from "@/entities/entities";
import { useState } from "react";
import { SubmitHandler, useForm } from "react-hook-form";

export default function cadastro() {
  // Aqui ficam function, estados (usamos o useState para isso), efeitos colaterais (usamos useEffect para isso)
    
  function validarCadastro(email: string, senha: string): CadastroErrors {
    const erros: CadastroErrors = {}; // cria um objeto do tipo CadastroErrors vazio

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // Validação do e-mail
    if (!email.trim()) { // .trim() retira espaço inicial e final da string
      erros.email = "O e-mail é obrigatório.";
    } else if (!emailRegex.test(email)) {
      erros.email = "Digite um e-mail válido.";
    }

    // Validação da senha
    if (!senha) {
      erros.senha = "A senha é obrigatória.";
    } else if (senha.length < 8) {
      erros.senha = "A senha deve ter pelo menos 8 caracteres.";
    }
    // verificar se a senha é numerica

    return erros;
  }
  
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UsuarioForm>({
  // resolver: ,
    defaultValues: {
      nome: "",
      email: "",
      senha: "", // 
    },
    mode: "onChange", // Atualizará valores conforme cada caractere digitado, o que permite que mensagens de erro nao sejam exibidas só ao enviar o form
  });

  const onSubmit = async (data: UsuarioForm) => {
    console.log("Enviando formulario", data);
    
    try {
    const resposta = await fetch("http://localhost:8080/usuarios", {
      // Isso enviará para o nosso back, no formato JSON, os dados do nosso formulario (nome e email), quando o usuario clicar em cadastrar
      method: "POST", // Esse metodo diz que queremos inserir no banco. Existe, por exemplo o metodo GET (que fala que queremos pegar uma informação do banco)
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome: data.nome, email: data.email }),
    });

    if (resposta.ok) {
      alert("Usuário cadastrado!");
    } else {
      alert("Erro ao cadastrar");
    }


      // setMostrarSpam(true);
    } catch (err) {
      console.error("Erro inesperado:", err);
      toast.error("Erro inesperado. Tente novamente.");
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
        onSubmit={Cadastrar}
      >
        <div className="flex flex-col gap-2">
          <label className="font-medium">Nome</label>

          <input
            className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
            type="text"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
          />
        </div>

        <div className="flex flex-col gap-2">
          <label className="font-medium">E-mail</label>

          <input
            className="rounded-md border border-gray-300 p-2 outline-none focus:border-purple-600"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <button
          className="rounded-md bg-purple-600 p-2 font-medium text-white hover:bg-purple-700"
          type="submit"
        >
          Cadastrar
        </button>
      </form>
    </main>
  );
}
