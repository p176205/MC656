"use client";

import Navbar from "@/components/navbar";
import { Contexto, ContextoDTO, Usuario } from "@/entities/entities";
import { useState, useEffect } from "react";

export default function Home() {
  const [carregando, setCarregando] = useState(true);
  const [user, setUser] = useState<Usuario>();
  const [contextos, setContextos] = useState<ContextoDTO[]>([]);

  useEffect(() => {
    async function carregarDados() {
      setCarregando(true);

      try {
        // Testando o metodo GET
        const resposta = await fetch(
          `http://localhost:8080/api/contextos/${3}`, // gente aqui esta hardcoded pq estava apenas testando com um id que esta no meu banco
          {
            method: "GET",
            headers: { "Content-Type": "application/json" },
          },
        );

        if (!resposta.ok) {
          const erro = await resposta.text();
          console.error("Erro ao buscar contextos:", erro);
          return;
        }

        const contextos: ContextoDTO[] = await resposta.json();

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

  // front basico só para teste
  return (
    <main className="text-black">
      <h1>Meus contextos</h1>

      {carregando ? (
        <p>Carregando contextos...</p>
      ) : contextos.length === 0 ? (
        <p>Nenhum contexto encontrado.</p>
      ) : (
        <ul>
          {contextos.map((contexto) => (
            <li key={contexto.id}>
              <div>{contexto.nome}</div>
              <div>{contexto.papel}</div>
              <div>{contexto.tipo}</div>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}

// Gente aqui é só uns rascunhos de algumas coisas que pensei
// so estava pensando mais ou menos em como vai ficar
// <main className="min-h-screen bg-gray-50 text-gray-900">
//     <header className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-6 py-6">
//         <div>
//             <h1 className="text-2xl font-bold">
//               Olá, usuário!
//             </h1>
//             <p className="mt-1 text-sm text-gray-500">
//               Você está no contexto "X".
//             </p>
//         </div>
//        {/*<SeletorContexto/>*/}
//     </header>

//     <div className="mx-auto max-w-6xl px-6">
//         {/* <Navbar/> */}

//         {/* <div className="py-8">{renderizarConteudo()}</div> */}
//     </div>
// </main>

// ----------------------------------------------------------------------------------------------------

// import { useState } from "react";

// export default function Cadastro() {
//     const [loading, setLoading] = useState(false);

//     async function criarAssembleia() {
//         setLoading(true);

//         try {
//             const resposta = await fetch(
//                 "http://localhost:8080/api/contextos/assembleias",
//                 {
//                     method: "POST",
//                     headers: {
//                         "Content-Type": "application/json",
//                     },
//                     body: JSON.stringify({
//                         nome: "C1",
//                         participantes: 3,
//                     }),
//                 }
//             );

//             // Lê o corpo uma única vez, independentemente do resultado.
//             const texto = await resposta.text();

//             if (resposta.ok) {
//                 console.log("Assembleia criada:", texto);
//             } else {
//                 console.error(
//                     "Erro do backend:",
//                     resposta.status,
//                     texto
//                 );
//             }
//         } catch (err) {
//             console.error("Erro inesperado:", err);
//         } finally {
//             setLoading(false);
//         }
//     }

//     return (
//         <section>
//             <button
//                 onClick={criarAssembleia}
//                 disabled={loading}
//                 className="bg-black"
//             >
//                 {loading ? "Criando..." : "Criar assembleia"}
//             </button>
//         </section>
//     );
// }
