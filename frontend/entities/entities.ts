export type Usuario = {
    id?: string; // quando recebermos do back no futuro precisaremos desse campo. Esse ponto de interrogação diz que é opcional
    nome: string;
    email: string;
    senha: number; 
};

export type UsuarioForm = {
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