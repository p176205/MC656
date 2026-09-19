export type Usuario = {
    id?: string; // quando recebermos do back no futuro precisaremos desse campo. Esse ponto de interrogação diz que é opcional
    nome: string;
    email: string;
    senha: number; 
};

export type UsuarioForm = {
    id?: string; // quando recebermos do back no futuro precisaremos desse campo. Esse ponto de interrogação diz que é opcional
    nome: string;
    email: string;
    senha: string; 
};


export type CadastroErrors = {
  email?: string;
  senha?: string;
};