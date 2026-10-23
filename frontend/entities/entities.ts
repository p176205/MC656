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

export interface Assembleia{
    id: number;
    nome: string;
    participantes: number;
}

export interface Condominio{
    id: number;
    nome: string;
    total_moradores: number;
}

export interface CentroAcademico{
    id: number;
    nome: string;
    tempo_vigencia: number;
}

export interface Contexto{
    tipo: Condominio | CentroAcademico | Assembleia;
}