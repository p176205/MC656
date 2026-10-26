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


export interface UsuarioResponse{
    id: number;
    nome: string;
    email: string;
}

export interface Assembleia {
    id: number;
    nome: string;
    participantes: number;
}

export interface Condominio {
    id: number;
    nome: string;
    total_moradores: number;
}

export interface CentroAcademico {
    id: number;
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