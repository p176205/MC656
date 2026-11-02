"use client";

import { ContextoDTO } from "@/entities/entities";
import {
  MdAdd,
  MdArrowOutward,
  MdCheck,
  MdFactCheck,
  MdKeyboardArrowDown,
  MdKeyboardArrowUp,
  MdOutlineClose,
} from "react-icons/md";

interface SeletorProps {
  contextos: ContextoDTO[];
  contextoAtual: ContextoDTO;
  open: boolean;
  setOpen: (estado: boolean) => void;
  onSelecionar: (contexto: ContextoDTO) => void;
  onCriar: () => void;
  onEntrar: () => void;
}

export default function SeletorContexto({
  contextos,
  contextoAtual,
  open,
  setOpen,
  onSelecionar,
  onCriar,
  onEntrar,
}: SeletorProps) {
  return (
    <div className="relative ">
      <button
        onClick={() => setOpen(!open)}
        className="w-40 h-12 flex items-center gap-2 rounded-xl border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-800 shadow-sm hover"
      >
        <span className="font-semibold text-gray-600">Selecione outro contexto</span>
        <span>{open ? <MdKeyboardArrowUp /> : <MdKeyboardArrowDown />}</span>
      </button>

      {open && (
        <div className="absolute right-0 z-50 mt-2 w-72 rounded-xl border border-gray-200 bg-white p-2 shadow-lg">
          <div className="flex flex-row gap-10">
            <p className="px-3 py-2 text-xs font-semibold uppercase text-gray-600">
              Seus contextos
            </p>

            <button
              className="flex flex-row gap-1 px-3 py-2 text-xs font-semibold uppercase text-red-500"
              onClick={() => {
                setOpen(false);
              }}
            >
              <div>Fechar </div>
              <MdOutlineClose color="red" size={16} />
            </button>
          </div>
          {contextos.map((contexto) => (
            <button
              key={contexto.id}
              onClick={() => {
                onSelecionar(contexto);
                setOpen(false);
              }}
              className={`w-full rounded-lg px-3 py-2 text-left text-sm hover:bg-blue-50 ${
                contextoAtual?.id === contexto.id
                  ? "bg-blue-50 font-semibold text-indigo-600"
                  : "text-gray-700"
              }`}
            >
              <div className="flex flex-row gap-3">
                {contextoAtual?.id === contexto.id ? <MdCheck /> : ""}
                {contexto.nome}
              </div>
            </button>
          ))}

          <div className="my-2 border-t border-gray-100" />

          <button
            onClick={() => {
              setOpen(false);
              onCriar();
            }}
            className="w-full rounded-lg px-3 py-2 text-left text-sm text-blue-600 hover:bg-blue-50"
          >
            <div className="flex flex-row gap-1">
              <MdAdd />
              <p>Entrar em um contexto existente</p>
            </div>
          </button>

          <button
            onClick={() => {
              setOpen(false);
              onEntrar();
            }}
            className="w-full rounded-lg px-3 py-2 text-left text-sm text-blue-600 hover:bg-blue-50"
          >
            <div className="flex flex-row gap-1">
              <MdArrowOutward />
              <p>Entrar em um contexto existente</p>
            </div>
          </button>
        </div>
      )}
    </div>
  );
}
