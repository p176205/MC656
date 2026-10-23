"use client";

type Aba = "comunidade" | "votacao" | "propostas" | "configurar-votacao";

interface NavbarProps {
  abaSelecionada: Aba;
  setAbaSelecionada: (aba: Aba) => void;
  ehAdmin: boolean;
  tipoContexto: string;
}

export default function Navbar({
  abaSelecionada,
  setAbaSelecionada,
  ehAdmin,
  tipoContexto,
}: NavbarProps) {

  const abas: { id: Aba; nome: string }[] = [
    { id: "comunidade", nome: "Comunidade" },
    { id: "votacao", nome: "Votação" },
    {
      id: tipoContexto === "assembleia" ? "propostas" : "propostas",
      nome: tipoContexto === "assembleia" ? "Chapas" : "Propostas",
    },
    ...(ehAdmin
      ? [{ id: "configurar-votacao" as Aba, nome: "Configurar votação" }]
      : []),
  ];

  return (
    <nav className="flex w-full gap-2 overflow-x-auto border-b border-gray-200">
      {abas.map((aba) => (
        <button
          key={aba.id}
          onClick={() => setAbaSelecionada(aba.id)}
          className={`shrink-0 border-b-2 px-4 py-3 text-sm font-medium transition-colors ${
            abaSelecionada === aba.id
              ? "border-blue-600 text-blue-600"
              : "border-transparent text-gray-500 hover:text-blue-600"
          }`}
        >
          {aba.nome}
        </button>
      ))}
    </nav>
  );
}