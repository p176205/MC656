
import { validarCadastro } from "../app/actions/cadastro";
import { Usuario } from "@/entities/entities";
import { describe, it, expect } from "vitest";

describe("Validação do nome", () => {
  it("deve rejeitar nome vazio", () => {
    const usuario: Usuario = {
      nome: "",
      email: "teste@email.com",
      senha: "123456",
    };

    const errors = validarCadastro(usuario);

    expect(errors.nome.status).toBe(true);

    expect(errors.nome.message).toBe(
      "O nome é obrigatório",
    );
  });
    it("deve rejeitar com mais de 50 caracteres", () => {
    const usuario: Usuario = {
      nome: "A".repeat(51),
      email: "teste@email.com",
      senha: "123456",
    };

    const errors = validarCadastro(usuario);

    expect(errors.nome.status).toBe(true);

    expect(errors.nome.message).toBe(
      "O nome deve ter do máximo 50 caracteres",
    );
  });
});

describe("Validação do email", () => {
  it("deve rejeitar email vazio", () => {
    const usuario: Usuario = {
      nome: "Teste",
      email: "",
      senha: "123456",
    };

    const errors = validarCadastro(usuario);

    expect(errors.email.status).toBe(true);

    expect(errors.email.message).toBe(
      "O e-mail é obrigatório",
    );
  });
  it("deve rejeitar email inválido", () => {
    const usuario: Usuario = {
      nome: "Teste",
      email: "email-invalido",
      senha: "123456",
    };

    const errors = validarCadastro(usuario);

    expect(errors.email.status).toBe(true);

    expect(errors.email.message).toBe(
      "O e-mail informado é inválido",
    );
  });
});

describe("Validação da senha", () => {
  it("deve rejeitar senha vazia", () => {
    const usuario: Usuario = {
      nome: "Teste",
      email: "teste@email.com",
      senha: "",
    };

    const errors = validarCadastro(usuario);

    expect(errors.senha.status).toBe(true);

    expect(errors.senha.message).toBe(
      "A senha é obrigatória",
    );
  });
    it("deve rejeitar senha com mais de 6 caracteres", () => {
    const usuario: Usuario = {
      nome: "Teste",
      email: "teste@email.com",
      senha: "1234567",
    };

    const errors = validarCadastro(usuario);

    expect(errors.senha.status).toBe(true);

    expect(errors.senha.message).toBe(
      "A senha deve conter exatamente 6 caracteres",
    );
  });
});

describe("Validação do formulário completo", () => {
  it("deve aceitar usuário válido", () => {
    const usuario: Usuario = {
      nome: "Priscilla Almeida",
      email: "priscilla@email.com",
      senha: "123456",
    };

    const errors = validarCadastro(usuario);

    expect(errors.nome.status).toBe(false);
    expect(errors.email.status).toBe(false);
    expect(errors.senha.status).toBe(false);
  });
});

describe("Validação do formulário completo", () => {
  it("deve rejeitar usuário inválido", () => {
    const usuario: Usuario = {
      nome: "P".repeat(50),
      email: "@email.com",
      senha: "12345",
    };

    const errors = validarCadastro(usuario);

    expect(errors.nome.status).toBe(false);

    expect(errors.email.status).toBe(true);
    expect(errors.email.message).toBe(
      "O e-mail informado é inválido",
    );

    expect(errors.senha.status).toBe(true);
    expect(errors.senha.message).toBe(
      "A senha deve conter exatamente 6 caracteres",
    );
  });
});



// Explicação (instalação):
// - instalei o Vitest usando: npm install -D vitest

// - No package.json, adicione no script:
//     "test": "vitest",
//     "test:run": "vitest run"

// - Quando executarmos no terminal "npm run test" ou "npm run test:run"

//Explicação código:
// | Função     | Responsabilidade              |
// | ---------- | ----------------------------- |
// | `describe` | Agrupa testes relacionados    |
// | `it`       | Define um caso de teste       |
// | `expect`   | Verifica o resultado esperado |
