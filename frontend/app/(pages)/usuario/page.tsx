"use client";

import { useState } from "react";

export default function usuario() {
  // Aqui ficam function, estados (usamos o useState para isso), efeitos colaterais (usamos useEffect para isso)
  const [nome, setNome] = useState(""); // Isso não é um array (mesmo que pareça) é um estado (pense como se fosse uma variavel), usamos desse jeito para que na tela apareça a mudança quando essa "variavel" mudar
  const [email, setEmail] = useState("");

  async function Cadastrar() {
    const resposta = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/usuarios`, { // Isso enviará para o nosso back, no formato JSON, os dados do nosso formulario (nome e email), quando o usuario clicar em cadastrar
      method: "POST", // Esse metodo diz que queremos inserir no banco. Existe, por exemplo o metodo GET (que fala que queremos pegar uma informação do banco)
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nome: nome, email: email }),
    });

    if (resposta.ok) {
      alert("Usuário cadastrado!");
      setNome(""); // reseta o estado (variavel) nome
      setEmail("");
    } else {
      alert("Erro ao cadastrar");
    }
  }

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
