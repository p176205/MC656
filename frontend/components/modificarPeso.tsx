
"use client";

import { useState } from "react";
import { MdOutlineClose } from "react-icons/md";
import { toast } from "sonner";
import { alterarPeso } from "@/services/apiFacade";
import { Membro } from "@/entities/entities";

interface ModificarPesoProps {
  membro: Membro;
  contextoAtual: number;
  setModificarPeso: (estado: boolean) => void;
}

export default function ModificarPeso({
  membro,
  contextoAtual,
  setModificarPeso,
}: ModificarPesoProps) {

  const [peso, setPeso] = useState(String(membro.peso ?? 1));
  const [salvando, setSalvando] = useState(false);

  const salvarPeso = async () => {
    const novoPeso = Number(peso);

    if (!peso || !Number.isFinite(novoPeso) || novoPeso <= 0) {
      toast.error("O peso deve ser maior que zero.");
      return;
    }

    setSalvando(true);

    try {
      await alterarPeso(contextoAtual, membro.id, novoPeso);

      toast.success("Peso atualizado com sucesso!");
      setModificarPeso(false);

    } catch (error) {
      console.error("Erro ao alterar peso:", error);

      if (error instanceof Error) {
        toast.error(error.message);
      } else {
        toast.error("Erro ao alterar peso do membro.");
      }

    } finally {
      setSalvando(false);
    }
  };

  return (
    <div className="max-w-xl w-full mx-auto p-6 bg-white border-r-8 border-r-indigo-500 rounded-xl shadow-md border border-gray-100 my-8">

      {/* Cabeçalho */}
      <div className="flex items-center justify-between mb-8">
        <span className="text-sm font-semibold text-indigo-500 bg-indigo-50 px-3 py-1 rounded-full">
          Editar peso
        </span>

        <button
          type="button"
          className="flex flex-row gap-1 px-3 py-2 text-xs font-semibold uppercase text-red-500"
          onClick={() => setModificarPeso(false)}
        >
          <div>Fechar</div>
          <MdOutlineClose color="red" size={16} />
        </button>
      </div>

      {/* Conteúdo */}
      <div className="min-h-[160px] space-y-5">

        <div>
          <h2 className="text-xl font-bold text-gray-800">
            Modificar peso do membro
          </h2>

          <p className="text-sm text-gray-500 mt-1">
            Altere o peso de participação de {membro.nome}.
          </p>
        </div>

        <div className="flex flex-col gap-1.5">
          <label className="font-semibold text-sm text-indigo-900">
            Peso atual: {membro.peso}
          </label>

          <input
            className="rounded-xl border border-indigo-200 bg-white/80 p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
            type="number"
            min="0.01"
            step="0.01"
            value={peso}
            onChange={(e) => setPeso(e.target.value)}
          />
        </div>
      </div>

      <div className="flex items-center justify-center gap-4 pt-6">

        <button
          type="button"
          onClick={salvarPeso}
          disabled={salvando}
          className="mt-4 w-1/3 rounded-xl bg-indigo-500 p-3.5 font-semibold text-white shadow-lg shadow-indigo-500/30 transition-all duration-200 hover:bg-indigo-700 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {salvando ? "Salvando..." : "Salvar"}
        </button>

      </div>
    </div>
  );
}
