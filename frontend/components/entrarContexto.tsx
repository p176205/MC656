"use client";

import {
  UsuarioResponse,
} from "@/entities/entities";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { MdOutlineClose } from "react-icons/md";

interface EntrarContextoProps {
  user: UsuarioResponse;
  setEntrarContexto: (estado: boolean) => void;
}

interface EntrarContextoFormData {
  link: string;
}

export default function EntrarContexto({
  user,
  setEntrarContexto,
}: EntrarContextoProps) {
  const [entrar, setEntrar] = useState<boolean>(false);

  const { register, handleSubmit } = useForm<EntrarContextoFormData>({
    defaultValues: {
      link: "",
    },
    mode: "onChange",
  });

  const onSubmit = async (data: EntrarContextoFormData) => {
    console.log("Enviando formulario", data.link);
    setEntrar(true);

    try {
      //   const resposta: ContextoDTO = await entrarContexto(link, user.id);
      //   if (resposta) {
      //     toast.success(`Você entrou no contexto`);
      //   }
    } catch (err) {
      console.error("Erro inesperado:", err);
    } finally {
      setEntrarContexto(false);
      setEntrar(false);
      window.location.reload();
    }
  };

  return (
    <div className="max-w-xl w-full mx-auto p-6 bg-white rounded-xl shadow-md border border-gray-100 my-8">
      {/* Indicador de Progresso */}
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center space-x-2">
          <span className="text-sm font-semibold text-indigo-500 bg-indigo-50 px-3 py-1 rounded-full">
            Passo 1 de 1
          </span>
        </div>
        <div className="flex space-x-1 h-2 w-8 rounded-full transition-colors duration-300 bg-indigo-500"></div>
        <button
          className="flex flex-row gap-1 px-3 py-2 text-xs font-semibold uppercase text-red-500"
          onClick={() => {
            setEntrarContexto(false);
          }}
        >
          <div>Fechar </div>
          <MdOutlineClose color="red" size={16} />
        </button>
      </div>

      {/* O <form> envolve toda a etapa 2 para que o botão de submit funcione corretamente */}
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="min-h-[200px]">
          <div className="space-y-4 animate-fadeIn">
            <h2 className="text-xl font-bold text-gray-800">
              Insira o link do contexto
            </h2>
            <div className="flex flex-col gap-1.5">
              <label className="font-semibold text-sm text-indigo-900">
                Link:
              </label>
              <input
                className="rounded-xl border border-indigo-200 bg-white/80 backdrop-blur-sm p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
                type="text"
                {...register("link")}
              />
            </div>
          </div>
          <div className="flex items-center justify-center pt-6">
            <button
              className="mt-4 w-1/3 rounded-xl bg-indigo-500 p-3.5 font-semibold text-white shadow-lg shadow-indigo-500/30 transition-all duration-200 hover:bg-indigo-700 hover:shadow-indigo-500/40 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
              type="submit"
              disabled={entrar}
            >
              {entrar ? "Entrando..." : "Entrar"}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
