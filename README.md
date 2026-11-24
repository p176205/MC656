# MC656 — Plataforma de Deliberações e Eleições

Sistema web para organizar votações e deliberações em assembleias, centros
acadêmicos e condomínios, incluindo eleições por chapas.

## Alunos Integrantes

| NOME | RA |
| -------- | -------- |
| Daniela Souza Cardoso Naves | 281141   |
| Thiago Gradvohl de Oliveira | 281378   |
| Gabriel Lopes de Barros     | 281198   |
| Priscilla Almeida           | 176205   |
| Ana Luiza Mota Gomes        | 242389   |

## Descrição

O sistema abrange três contextos principais: eleições de Centros Acadêmicos,
eleições e deliberações condominiais e assembleias. O objetivo é oferecer um
ambiente centralizado, organizado e transparente para interação antes, durante
e após processos eleitorais ou deliberativos.

## Pré-requisitos

- Git;
- Java 21 (o backend declara `java.version` 21 no `pom.xml`);
- Node.js 20 ou superior e npm;
- Docker Compose (para executar o PostgreSQL localmente).

O backend usa Maven por meio do Maven Wrapper versionado em `backend/`. O
frontend usa Next.js e npm, com as versões das dependências registradas em
`frontend/package-lock.json`.
O arquivo `frontend/.nvmrc` fixa a versão de Node recomendada para o frontend;
com nvm, execute `nvm use` dentro de `frontend/`.

## Clone

Substitua `URL_DO_REPOSITORIO` pela URL do repositório:

```bash
git clone URL_DO_REPOSITORIO
cd MC656
```

## Variáveis de ambiente

Crie o arquivo abaixo na raiz do projeto:

```bash
.env
```

Com as seguintes variáveis de ambiente:

```env
DB_URL=jdbc:postgresql://localhost:5432/mc656
DB_USERNAME=mc656_user
DB_PASSWORD=mc656_dev
NEXT_PUBLIC_API_URL=http://localhost:8080
```
Observação: O arquivo `.env` não deve ser versionado.

O Spring Boot consome `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` por meio das
propriedades `spring.datasource.*`. A variável `NEXT_PUBLIC_API_URL` é
utilizada pelo frontend no navegador e deve estar definida antes do build do
Next.js.

O Spring Boot não carrega automaticamente arquivos `.env`. No Linux, macOS,
WSL ou Git Bash, exporte as variáveis antes de iniciar o backend:

```bash
set -a
. ./.env
set +a
```

## Banco de dados com Docker Compose

Na raiz do projeto, inicie o PostgreSQL:

```bash
docker compose up -d db
```

O Compose lê as configurações do `.env` e persiste os dados no volume
`db-data`. Se não houver `.env`, o banco usa `mc656_user` e `mc656_dev` como
valores padrão de desenvolvimento.

Para usar um PostgreSQL já instalado, não inicie o serviço `db`; configure
`DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no `.env` para apontar à sua
instalação.

## Preparação automatizada

A partir da raiz do projeto, execute:

```bash
sh setup.sh
```

O script:

- verifica Java 21, Node.js e npm;
- executa `npm ci` em `frontend/`;
- usa `backend/mvnw` para executar `dependency:go-offline` e preparar as
  dependências Maven.

O script não instala Java, Node.js ou npm, não configura variáveis de ambiente
e não instala nem configura PostgreSQL. Em Windows, execute-o pelo Git Bash ou
WSL.

## Execução do backend

Com o PostgreSQL disponível, exporte as variáveis do `.env` e inicie o
backend:

```bash
cd backend
./mvnw spring-boot:run
```

No Windows, também é possível usar o wrapper correspondente:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

O backend é executado em `http://localhost:8080` por padrão.

## Execução do frontend

Em outro terminal, exporte as variáveis do `.env` nesse terminal e execute:

```bash
cd frontend
npm run dev
```

O frontend é executado em `http://localhost:3000` por padrão.

## Comandos úteis

### Backend

```bash
cd backend
./mvnw test
./mvnw clean package
```

### Frontend

```bash
cd frontend
npm run test:run
npm run lint
npm run build
npm run start
```

O script `test:run` executa os testes automatizados do frontend sem modo
interativo.

## API de votações

As rotas abaixo são servidas pelo backend em `http://localhost:8080`. Os nomes
JSON seguem `camelCase`. Percentuais, quórum e peso do voto são números JSON.
`dataInicio` e `dataFim` são datas/hora
locais no formato ISO-8601 `yyyy-MM-dd'T'HH:mm:ss`, por exemplo
`2026-11-08T19:30:00`; não incluem fuso horário nem sufixo `Z`.

| Método e rota | Resultado |
| --- | --- |
| `GET /api/contextos/id/{contextoId}` | Retorna `id`, `nome` e `tipo` do contexto; 404 se inexistente. A rota `/api/contextos/{id}` existente mantém a listagem de contextos do usuário. |
| `GET /api/contextos/{contextoId}/membros` | Retorna a lista de membros (`id`, `nome`, `email`) participantes do contexto; 404 se inexistente. |
| `POST /api/votacoes` | Abre uma votação (`DELIBERACAO` ou `ELEICAO`) e retorna seus dados com HTTP 201; contexto inexistente retorna 404 e dados inválidos retornam 400. |
| `GET /api/votacoes/contexto/{contextoId}` | Retorna as votações daquele contexto; contexto inexistente retorna 404. |
| `GET /api/votacoes/{id}` | Retorna os dados de uma votação; 404 se inexistente. |
| `POST /api/votacoes/{id}/votos` | Registra o voto do usuário participante; conflito ou votação fechada retorna 409. |
| `PATCH /api/votacoes/{id}/encerrar` | Encerra uma votação aberta. |
| `GET /api/votacoes/{id}/apuracao` | Retorna a apuração depois do encerramento (ou do fim do prazo); para votação ainda aberta retorna 409. |

O cadastro recebe `titulo`, `descricao`, `contextoId`, `tipo` (`DELIBERACAO` ou `ELEICAO`), `regraAprovacao`,
`percentualMinimo`, `quorumMinimo`, `dataInicio`, `dataFim`, `adminId` e, quando `tipo == ELEICAO`, a lista de `chapas` (`nome`, `proposta` e `membrosIds`). As datas podem
ser omitidas pela API; quando informadas, a data final precisa ser posterior à
inicial. A interface de cadastro exige ambas. O status é controlado pelo
servidor: toda votação nova começa como `ABERTA`. Os únicos status são
`ABERTA` (aceita votos dentro da janela de datas) e `ENCERRADA` (não aceita
novos votos; apuração disponível).

As regras aceitas são `MAIORIA_SIMPLES` e `PERCENTUAL_MINIMO`. Para
`PERCENTUAL_MINIMO`, `percentualMinimo` deve ser maior que 0 e até 100; para
`MAIORIA_SIMPLES`, envie `null`. `quorumMinimo` fica entre 0 e 100, inclusive.

As respostas incluem `id`, `contextoNome`, `tipo`, `status` e `chapas`. A apuração expõe
`resultado`, `quorumAtingido`, contagens/pesos de votos,
`percentualSimObtido`, `resultadosChapas` e `chapaVencedora`.

## Limitações conhecidas

- O arquivo `.env` precisa ser carregado/exportado manualmente antes da
  execução; o `setup.sh` apenas prepara dependências.
- O teste de contexto do backend pode exigir um PostgreSQL acessível com as
  variáveis configuradas.
