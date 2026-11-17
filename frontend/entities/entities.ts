export type Usuario = {
    id?: string; // quando recebermos do back no futuro precisaremos desse campo. Esse ponto de interrogação diz que é opcional
    nome: string;
    email: string;
    senha: string; 
};

export type LoginUsuario = {
    id?: string; 
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

export type LoginErrors = {
    email: Error;
    senha: Error;
};


export interface UsuarioResponse{
    id: number;
    nome: string;
    email: string;
}

export interface Assembleia {
    id?: number;
    codigo?: string; // O codigo chegará do back como string
    nome: string;
    participantes: number;
}

export interface Condominio {
    id?: number;
    codigo?: string; // O codigo chegará do back como string
    nome: string;
    total_moradores: number;
}

export interface CentroAcademico {
    id?: number;
    codigo?: string; // O codigo chegará do back como string
    nome: string;
    tempo_vigencia: number;
}

// Representa os possíveis tipos de contexto.
export type Contexto = Assembleia | Condominio | CentroAcademico;

// Representa o campo tipo enviado pelo backend.
export type TipoContexto =
    | "ASSEMBLEIA"
    | "CONDOMINIO"
    | "CENTRO_ACADEMICO";

// Representa o campo papel enviado pelo backend
export type Papel =
    | "MEMBRO"
    | "INTEGRANTE_CHAPA"
    | "ADMINISTRADOR"
    | "CONDOMINO"
    | "SINDICO"
    | "PARTICIPANTE_ASSEMBLEIA"
    | "PROPONENTE"

// Representa cada item retornado pela API.
export interface ContextoDTO {
    id: number;
    nome: string;
    tipo: TipoContexto;
    papel: Papel;
    contexto: Contexto;
}

export interface ContextoDetalhe {
    id: number;
    nome: string;
    tipo: TipoContexto;
}

export type Aba =
  | "COMUNIDADE"
  | "VOTACAO"
  | "MEMBROS"

export type TipoVotacao =
  "DELIBERACAO"

export type RegraAprovacao =
  | "MAIORIA_SIMPLES"
  | "PERCENTUAL_MINIMO";

export type StatusVotacao =
  | "ENCERRADA"
  | "ABERTA"

export type VotacaoCadastro = {
    titulo: string;
    descricao: string;
    contextoId: number;
    regraAprovacao: RegraAprovacao;
    percentualMinimo: number | null;
    quorumMinimo: number;
    dataInicio: string;
    dataFim: string;
} & (
    | { pauta: string; participantesAptos: number; pesoVoto?: never; unidadeApartamento?: never; categoriaSegmento?: never; elegibilidade?: never }
    | { pesoVoto: number; unidadeApartamento: string; pauta?: never; participantesAptos?: never; categoriaSegmento?: never; elegibilidade?: never }
    | { categoriaSegmento: string; elegibilidade: string; pauta?: never; participantesAptos?: never; pesoVoto?: never; unidadeApartamento?: never }
);

export interface VotacaoResponse {
    id: number;
    titulo: string;
    descricao: string;
    contextoId: number;
    contextoNome: string;
    tipo: TipoVotacao;
    regraAprovacao: RegraAprovacao;
    percentualMinimo: number | null;
    quorumMinimo: number;
    status: StatusVotacao;
    dataInicio: string | null;
    dataFim: string | null;
    pauta: string | null;
    participantesAptos: number | null;
    pesoVoto: number | null;
    unidadeApartamento: string | null;
    categoriaSegmento: string | null;
    elegibilidade: string | null;
}

export interface ApuracaoResponse {
    votacaoId: number;
    titulo: string;
    resultado: "APROVADA" | "REJEITADA" | "SEM_QUORUM";
    quorumAtingido: boolean;
    votosSim: number;
    votosNao: number;
    abstencoes: number;
    totalVotantes: number;
    regraAprovacao: RegraAprovacao;
    percentualMinimo: number | null;
    percentualSimObtido: number | null;
}