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

export type StatusVotacao = "AGENDADA" | "EM_ANDAMENTO" | "ENCERRADA" | "CANCELADA";

export type VotacaoCadastro = {
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

export type VotacaoResponse = VotacaoCadastro & {
    id: number;
};