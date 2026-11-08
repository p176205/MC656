"use client";

import { Aba} from "@/entities/entities";

interface NavbarProps {
  abaSelecionada: Aba;
  setAbaSelecionada: (aba: Aba) => void;
}

export default function Navbar({
  abaSelecionada,
  setAbaSelecionada,
}: NavbarProps) {

  const abas: { id: Aba; nome: string }[] = [
    { id: "COMUNIDADE", nome: "Comunidade" },
    { id: "VOTACAO", nome: "Votações" },
    { id: "MEMBROS", nome: "Membros" },
  ];

  return (
    <nav className="flex w-full gap-2 overflow-x-auto border-b border-gray-200">
      {abas.map((aba) => (
        <button
          key={aba.id}
          onClick={() => setAbaSelecionada(aba.id)}
          className={`shrink-0 border-b-2 px-4 py-3 text-sm font-medium transition-colors ${
            abaSelecionada === aba.id
              ? "border-indigo-600 text-indigo-600"
              : "border-transparent text-gray-500 hover:text-indigo-600"
          }`}
        >
          {aba.nome}
        </button>
      ))}
    </nav>
  );
}