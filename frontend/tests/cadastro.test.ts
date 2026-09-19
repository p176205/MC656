
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
