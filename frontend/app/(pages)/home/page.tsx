
"use client";

import { useState } from "react";

export default function Cadastro() {
    const [loading, setLoading] = useState(false);

    async function criarAssembleia() {
        setLoading(true);

        try {
            const resposta = await fetch(
                "http://localhost:8080/api/contextos/assembleias",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        nome: "C1",
                        participantes: 3,
                    }),
                }
            );

            // Lê o corpo uma única vez, independentemente do resultado.
            const texto = await resposta.text();

            if (resposta.ok) {
                console.log("Assembleia criada:", texto);
            } else {
                console.error(
                    "Erro do backend:",
                    resposta.status,
                    texto
                );
            }
        } catch (err) {
            console.error("Erro inesperado:", err);
        } finally {
            setLoading(false);
        }
    }

    return (
        <section>
            <button
                onClick={criarAssembleia}
                disabled={loading}
                className="bg-black"
            >
                {loading ? "Criando..." : "Criar assembleia"}
            </button>
        </section>
    );
}
