import { CadastroErrors, Usuario } from "@/entities/entities";

export function validarCadastro(usuarioForm: Usuario): CadastroErrors {
    const errors: CadastroErrors = {
      nome: { message: "", status: false },
      email: { message: "", status: false },
      senha: { message: "", status: false },
    }; // cria um objeto do tipo CadastroErrors vazio
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // Validação do nome
    if (!usuarioForm.nome.trim()) {
      errors.nome = {
        message: "O nome é obrigatório",
        status: true,
      };
    } else if (usuarioForm.nome.trim().length > 50) {
      errors.nome = {
        message: "O nome deve ter do máximo 50 caracteres",
        status: true,
      };
    }

    // Validação do e-mail
    if (!usuarioForm.email.trim()) {
      // .trim() retira espaço inicial e final da string
      errors.email = {
        message: "O e-mail é obrigatório",
        status: true,
      };
    } else if (!emailRegex.test(usuarioForm.email)) {
      errors.email = {
        message: "O e-mail informado é inválido",
        status: true,
      };
    }

    // Validação da senha
    if (!usuarioForm.senha.trim()) {
      errors.senha = {
        message: "A senha é obrigatória",
        status: true,
      };
    } else if (usuarioForm.senha.trim().length != 6) {
      errors.senha = {
        message: "A senha deve conter exatamente 6 caracteres",
        status: true,
      };
    }
    return errors;
  }