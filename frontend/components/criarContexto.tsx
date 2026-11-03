"use client";

import {
  Assembleia,
  CentroAcademico,
  Condominio,
  Contexto,
  TipoContexto,
} from "@/entities/entities";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { FiArrowLeft, FiArrowRight, FiCheckCircle } from "react-icons/fi";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

interface CriarContextoProps {}

export default function CriarContexto({}: CriarContextoProps) {
  const router = useRouter();
  const [cadastrarContexto, setCadastrarContexto] = useState<boolean>(false);
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedContexto, setSelectedContexto] = useState<TipoContexto | null>(
    null,
  );

  interface ContextoFormData {
    tipo: TipoContexto | null;
    nome: string;
    participantes?: number;
    total_moradores?: number;
    tempo_vigencia?: number;
  }

  const { register, handleSubmit, setValue } = useForm<ContextoFormData>({
    defaultValues: {
      tipo: null,
      nome: "",
      participantes: 0,
      total_moradores: 0,
      tempo_vigencia: 0,
    },
    mode: "onChange",
  });

  // Funções de navegação atualizadas para sincronizar o setValue do formulário
  const handleNext = () => {
    if (selectedContexto) {
      setValue("tipo", selectedContexto); // Sincroniza o tipo escolhido com o useForm
    }
    if (currentStep < 2) setCurrentStep((prev) => prev + 1);
  };

  const handlePrev = () => {
    if (currentStep > 1) {
      setCurrentStep((prev) => prev - 1);
    }
  };

  const onSubmit = async (data: ContextoFormData) => {
    console.log("Enviando formulario", data);
    setCadastrarContexto(true);

    let contexto: Contexto;

    if (data.tipo === "ASSEMBLEIA") {
      contexto = {
        nome: data.nome.trim(),
        participantes: Number(data.participantes) || 0,
      } as Assembleia;
    } else if (data.tipo === "CONDOMINIO") {
      contexto = {
        nome: data.nome.trim(),
        total_moradores: Number(data.total_moradores) || 0,
      } as Condominio;
    } else {
      contexto = {
        nome: data.nome.trim(),
        tempo_vigencia: Number(data.tempo_vigencia) || 0,
      } as CentroAcademico;
    }

    console.log("contexto final:", contexto);

    try {
      const resposta = await criarContexto(contexto, data.tipo);
      if (resposta.ok) {
        toast.success(`contexto ${contexto.nome} criado`);
        router.refresh();
      }
    } catch (err) {
      console.error("Erro inesperado:", err);
    } finally {
      setCadastrarContexto(false);
    }
  };

  return (
    <div className="max-w-xl w-full mx-auto p-6 bg-white rounded-xl shadow-md border border-gray-100 my-8">
      {/* Indicador de Progresso */}
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center space-x-2">
          <span className="text-sm font-semibold text-indigo-500 bg-indigo-50 px-3 py-1 rounded-full">
            Passo {currentStep} de 2
          </span>
        </div>
        <div className="flex space-x-1">
          {[1, 2].map((step) => (
            <div
              key={step}
              className={`h-2 w-8 rounded-full transition-colors duration-300 ${
                step <= currentStep ? "bg-indigo-500" : "bg-gray-200"
              }`}
            />
          ))}
        </div>
      </div>

      {/* O <form> envolve toda a etapa 2 para que o botão de submit funcione corretamente */}
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="min-h-[200px]">
          {currentStep === 1 && (
            <div className="space-y-4 animate-fadeIn">
              <h2 className="text-xl font-bold text-gray-800">
                Selecione o tipo de Contexto para prosseguir
              </h2>
              <p className="text-gray-600 text-sm">
                Escolha uma das opções abaixo:
              </p>

              <div className="grid grid-cols-1 gap-3 pt-2">
                {[
                  {
                    id: "CENTRO_ACADEMICO" as TipoContexto,
                    title: "Cadastrar novo Centro Acadêmico",
                    desc: "Esse contexto permite a criação de chapas, votação e comunidade",
                  },
                  {
                    id: "CONDOMINIO" as TipoContexto,
                    title: "Cadastrar novo Condominio",
                    desc: "Esse contexto permite votacoes de sindico e outras pautas",
                  },
                  {
                    id: "ASSEMBLEIA" as TipoContexto,
                    title: "Cadastrar nova Assembleia",
                    desc: "Esse contexto permite a criação de pautas e votação",
                  },
                ].map((item) => (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => setSelectedContexto(item.id)}
                    className={`p-4 text-left rounded-lg border transition-all flex items-center justify-between ${
                      selectedContexto === item.id
                        ? "border-indigo-500 bg-indigo-50/50 text-indigo-900 shadow-sm"
                        : "border-gray-200 hover:border-gray-300 text-gray-700"
                    }`}
                  >
                    <div>
                      <p className="font-medium">{item.title}</p>
                      <p className="text-xs text-gray-500 mt-0.5">
                        {item.desc}
                      </p>
                    </div>
                    {selectedContexto === item.id && <FiCheckCircle />}
                  </button>
                ))}
              </div>
            </div>
          )}

          {currentStep === 2 && (
            <div className="space-y-4 animate-fadeIn">
              <h2 className="text-gray-700 flex flex-col">
                <p>
                  Contexto selecionado:{" "}
                  <span className="font-semibold text-indigo-600 uppercase">
                    {selectedContexto}
                  </span>
                </p>
                <span className="text-sm text-gray-600">
                  Configure os dados
                </span>
              </h2>

              <div className="p-4 bg-white rounded-lg border border-indigo-500 text-sm text-gray-600">
                <div className="flex w-full flex-col gap-4 my-auto px-2">
                  <div className="flex flex-col gap-1.5">
                    <label className="font-semibold text-sm text-indigo-900">
                      Nome
                    </label>
                    <input
                      className="rounded-xl border border-indigo-200 bg-white/80 backdrop-blur-sm p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
                      type="text"
                      placeholder="Digite o nome do contexto"
                      {...register("nome", { required: true })}
                    />
                  </div>

                  {selectedContexto === "CONDOMINIO" ? (
                    <div className="flex flex-col gap-1.5">
                      <label className="font-semibold text-sm text-indigo-900">
                        Total de moradores
                      </label>
                      <input
                        className="rounded-xl border border-indigo-200 bg-white/80 backdrop-blur-sm p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
                        type="number"
                        {...register("total_moradores")}
                      />
                    </div>
                  ) : selectedContexto === "CENTRO_ACADEMICO" ? (
                    <div className="flex flex-col gap-1.5">
                      <label className="font-semibold text-sm text-indigo-900">
                        Tempo de vigência
                      </label>
                      <input
                        className="rounded-xl border border-indigo-200 bg-white/80 backdrop-blur-sm p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
                        type="number"
                        {...register("tempo_vigencia")}
                      />
                    </div>
                  ) : (
                    <div className="flex flex-col gap-1.5">
                      <label className="font-semibold text-sm text-indigo-900">
                        Participantes
                      </label>
                      <input
                        className="rounded-xl border border-indigo-200 bg-white/80 backdrop-blur-sm p-3 text-slate-800 outline-none transition-all duration-200 focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-400/20"
                        type="number"
                        {...register("participantes")}
                      />
                    </div>
                  )}

                  <div className="flex items-center justify-center">
                    <button
                      className="mt-4 w-1/2 rounded-xl bg-indigo-500 p-3.5 font-semibold text-white shadow-lg shadow-indigo-500/30 transition-all duration-200 hover:bg-indigo-700 hover:shadow-indigo-500/40 active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
                      type="submit"
                      disabled={cadastrarContexto}
                    >
                      {cadastrarContexto ? "Cadastrando..." : "Cadastrar"}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </form>

      {/* Botões de Rodapé / Navegação por Etapas */}
      <div className="flex items-center justify-between pt-6 mt-6 border-t border-gray-100">
        {currentStep === 1 ? (
          <button
            type="button"
            onClick={handleNext}
            disabled={!selectedContexto}
            className={`px-5 py-2 text-sm font-medium text-white rounded-lg transition-all flex items-center space-x-2 ${
              !selectedContexto
                ? "bg-indigo-300 cursor-not-allowed"
                : "bg-indigo-500 hover:bg-indigo-700 shadow-sm"
            }`}
          >
            <span>Continuar</span>
            <FiArrowRight />
          </button>
        ) : (
          <div />
        )}

        {currentStep === 2 ? (
          <button
            type="button"
            onClick={handlePrev}
            className="px-4 py-2 text-sm font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors flex items-center space-x-2"
          >
            <FiArrowLeft />
            <span>Voltar</span>
          </button>
        ) : (
          <div />
        )}
      </div>
    </div>
  );
}
