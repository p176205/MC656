# Arquitetura do sistema de votação

## Componentes e responsabilidades

| Componente | Responsabilidade | Se relaciona com |
| --- | --- | --- |
| Frontend (Next.js/React) | Telas de cadastro e consulta. `services/apiFacade.ts` concentra as chamadas HTTP. | Backend, por HTTP/JSON |
| `UsuarioController` | Expõe `POST /api/usuarios`, valida o DTO de entrada e converte erro de regra em HTTP 400. | `UsuarioService` |
| `ParticipacaoController` | Expõe `GET /api/usuarios/{id}/contextos` e converte `Participacao` em `ContextoParticipacaoDTO`. | `ParticipacaoRepository` (direto, sem Service) |
| `UsuarioService` | Regras do cadastro: e-mail único e codificação da senha. | `UsuarioRepository`, `PasswordEncoder` |
| `PasswordConfig` / `PasswordEncoder` | Define a estratégia de hash de senha (BCrypt). | `UsuarioService` |
| Repositories (`Usuario`, `Participacao`, `CentroAcademico`, `Condominio`, `Assembleia`) | Acesso ao banco via Spring Data JPA. | PostgreSQL |
| Entidades (`Usuario`, `Participacao`, `Contexto` e suas subclasses) | Dados persistidos e regras de integridade. | Repositories |
| DTOs | Formato dos dados que entram e saem da API. | Controllers e Service |
| PostgreSQL | Armazena usuários, contextos e participações. | Repositories |

Os repositories de `CentroAcademico`, `Condominio` e `Assembleia` já existem, mas ainda não têm Controller nem Service que os usem.