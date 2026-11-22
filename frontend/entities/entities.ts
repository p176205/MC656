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
  | "DELIBERACAO"
  | "ELEICAO";

export type RegraAprovacao =
  | "MAIORIA_SIMPLES"
  | "PERCENTUAL_MINIMO";

export type StatusVotacao =
  | "ENCERRADA"
  | "ABERTA"

export interface MembroChapa {
    id: number;
    nome: string;
    email: string;
}

export interface Membro {
    id: number;
    nome: string;
    email: string;
    peso: number;
    papel: Papel;
}

export interface ChapaCadastro {
    nome: string;
    proposta: string | null;
    membrosIds: number[];
}

export interface ChapaResponse {
    id: number;
    votacaoId: number;
    nome: string;
    // Texto associado à chapa; não representa uma proposta independente.
    proposta: string | null;
    membros: MembroChapa[];
}

export type OpcaoVoto = "SIM" | "NAO" | "ABSTENCAO";

export type VotoCadastro =
    | { usuarioId: number; opcao: OpcaoVoto; chapaId?: never }
    | { usuarioId: number; chapaId: number; opcao?: never };

export type VotacaoCadastro = {
    titulo: string;
    descricao: string;
    contextoId: number;
    tipo: TipoVotacao;
    regraAprovacao: RegraAprovacao;
    percentualMinimo: number | null;
    quorumMinimo: number;
    dataInicio: string;
    dataFim: string;
    adminId: number;
    chapas: ChapaCadastro[] | null;
};

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
    chapas?: ChapaResponse[];
}

export interface ResultadoChapa {
    chapaId: number;
    nome: string;
    votos: number;
    percentualVotosValidos: number | null;
}

export interface ApuracaoResponse {
    votacaoId: number;
    titulo: string;
    resultado: "APROVADA" | "REJEITADA" | "SEM_QUORUM" | "ELEITA" | "EMPATE";
    quorumAtingido: boolean;
    votosSim: number;
    votosNao: number;
    abstencoes: number;
    totalVotantes: number;
    regraAprovacao: RegraAprovacao;
    percentualMinimo: number | null;
    percentualSimObtido: number | null;
    resultadosChapas?: ResultadoChapa[] | null;
    chapaVencedora?: ResultadoChapa | null;
}