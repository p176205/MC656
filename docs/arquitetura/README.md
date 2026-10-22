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

## Estilos arquiteturais

**Cliente-servidor.** O frontend é o cliente e envia requisições HTTP com JSON ao backend, que responde com o resultado. Assim cada lado evolui separadamente.

**Em camadas (no backend).** Controller recebe a requisição, Service concentra a regra de negócio e Repository acessa o banco. DTOs definem o que trafega pela API e entidades representam o que é persistido.

**Exceção conhecida.** `ParticipacaoController` acessa `ParticipacaoRepository` diretamente e faz a conversão para DTO. Isso pula a camada de Service e está registrado como code smell (Feature Envy) na Seção 3 do PDF.

## Decisões arquiteturais

**PostgreSQL como banco.** Modelo relacional com SQL e consultas complexas, adequado a usuários, contextos e participações. Em troca, exige manter um esquema e o desempenho depende da modelagem.

**Spring Boot no backend.** Facilita APIs REST e a separação em camadas. Em troca, traz mais configuração e abstrações do que alternativas simples.

**Next.js no frontend.** Organiza páginas e componentes sobre React e favorece reuso. Em troca, exige conhecimento adicional do framework.

**Herança JOINED para `Contexto`.** Centro Acadêmico, Condomínio e Assembleia herdam de `Contexto`, cada um com sua tabela ligada à tabela base. Isso permite uma plataforma única para os três cenários, com colunas específicas sem campos nulos. Em troca, consultas polimórficas exigem joins.