export type Usuario = {
    id?: string; // quando recebermos do back no futuro precisaremos desse campo. Esse ponto de interrogação diz que é opcional
    nome: string;
    email: string;
    senha: string; 
};

type Error = {
    message: string;
    status: boolean;
}

export type CadastroErrors = {
    nome: Error;
    email: Error;
    senha: Error;
};

export type RegraAprovacao =
    | "MAIORIA_SIMPLES"
    | "MAIORIA_ABSOLUTA"
    | "MAIORIA_QUALIFICADA"
    | "UNANIMIDADE";

export type StatusVotacao = "AGENDADA" | "ABERTA" | "EM_ANDAMENTO" | "ENCERRADA" | "CANCELADA";

export type TipoContexto = "ASSEMBLEIA" | "CONDOMINIO" | "CENTRO_ACADEMICO";

export type ContextoResponse = {
    id: number;
    nome: string;
    tipo: TipoContexto;
};

type VotacaoCadastroBase = {
    contextoId: number;
    nome: string;
    descricao: string;
    regraAprovacao: RegraAprovacao;
    percentualMinimo: number;
    quorumMinimo: number;
    dataInicio: string;
    dataFim: string;
    status: StatusVotacao;
};

type CamposAssembleia = {
    pauta: string;
    participantesAptos: number;
    pesoVoto?: never;
    unidadeApartamento?: never;
    categoriaSegmento?: never;
    elegibilidade?: never;
};

type CamposCondominio = {
    pauta?: never;
    participantesAptos?: never;
    pesoVoto: number;
    unidadeApartamento: string;
    categoriaSegmento?: never;
    elegibilidade?: never;
};

type CamposCentroAcademico = {
    pauta?: never;
    participantesAptos?: never;
    pesoVoto?: never;
    unidadeApartamento?: never;
    categoriaSegmento: string;
    elegibilidade: string;
};

export type VotacaoCadastro = VotacaoCadastroBase & (
    | CamposAssembleia
    | CamposCondominio
    | CamposCentroAcademico
);

export type VotacaoResponse = Omit<VotacaoCadastroBase, "status"> & {
    id: number;
    status: StatusVotacao;
    pauta: string | null;
    participantesAptos: number | null;
    pesoVoto: number | null;
    unidadeApartamento: string | null;
    categoriaSegmento: string | null;
    elegibilidade: string | null;
};