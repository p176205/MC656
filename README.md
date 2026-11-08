# MC656 - Podemos dar um título ao projeto

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
- Node.js e npm;
- PostgreSQL configurado localmente.

O backend usa Maven por meio do Maven Wrapper versionado em `backend/`. O
frontend usa Next.js e npm, com as versões das dependências registradas em
`frontend/package-lock.json`.

## Clone

Substitua `URL_DO_REPOSITORIO` pela URL do repositório:

```bash
git clone URL_DO_REPOSITORIO
cd MC656
```

## Variáveis de ambiente

Copie o arquivo de exemplo para criar a configuração local:

```bash
cp .env.example .env
```

O arquivo `.env` não deve ser versionado. Ajuste seus valores conforme a
instalação local do PostgreSQL:

```env
DB_URL=jdbc:postgresql://localhost:5432/example_db
DB_USERNAME=example_user
DB_PASSWORD=example_password
NEXT_PUBLIC_API_URL=http://localhost:8080
```

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

No PowerShell, defina as variáveis no processo atual, por exemplo:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/example_db"
$env:DB_USERNAME = "example_user"
$env:DB_PASSWORD = "example_password"
$env:NEXT_PUBLIC_API_URL = "http://localhost:8080"
```

## PostgreSQL

O PostgreSQL não é instalado nem configurado automaticamente. Crie
manualmente um banco e um usuário, ou use uma instalação PostgreSQL já
existente, e coloque no `.env` a URL, o usuário e a senha correspondentes.

Os valores de exemplo usam o banco `example_db`, o usuário `example_user` e a
porta `5432`. Eles são apenas exemplos e precisam existir na instalação local
ou ser substituídos por valores válidos.

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

Com as variáveis do banco exportadas, execute:

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

Em outro terminal, com `NEXT_PUBLIC_API_URL` definida, execute:

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
JSON seguem `camelCase`. Percentuais, quórum e peso do voto são números JSON;
`participantesAptos` é um inteiro. `dataInicio` e `dataFim` são datas/hora
locais no formato ISO-8601 `yyyy-MM-dd'T'HH:mm:ss`, por exemplo
`2026-11-08T19:30:00`; não incluem fuso horário nem sufixo `Z`.

| Método e rota | Resultado |
| --- | --- |
| `GET /api/contextos/id/{contextoId}` | Retorna `id`, `nome` e `tipo` do contexto; 404 se inexistente. A rota `/api/contextos/{id}` existente mantém a listagem de contextos do usuário. |
| `POST /api/votacoes` | Abre uma votação e retorna seus dados com HTTP 201; contexto inexistente retorna 404 e dados inválidos retornam 400. |
| `GET /api/votacoes/contexto/{contextoId}` | Retorna as votações daquele contexto; contexto inexistente retorna 404. |
| `GET /api/votacoes/{id}` | Retorna os dados de uma votação; 404 se inexistente. |
| `POST /api/votacoes/{id}/votos` | Registra o voto do usuário participante; conflito ou votação fechada retorna 409. |
| `PATCH /api/votacoes/{id}/encerrar` | Encerra uma votação aberta. |
| `GET /api/votacoes/{id}/apuracao` | Retorna a apuração depois do encerramento (ou do fim do prazo); para votação ainda aberta retorna 409. |

O cadastro recebe `titulo`, `descricao`, `contextoId`, `regraAprovacao`,
`percentualMinimo`, `quorumMinimo`, `dataInicio` e `dataFim`. As datas podem
ser omitidas pela API; quando informadas, a data final precisa ser posterior à
inicial. A interface de cadastro exige ambas. O status é controlado pelo
servidor: toda votação nova começa como `ABERTA`. Os únicos status são
`ABERTA` (aceita votos dentro da janela de datas) e `ENCERRADA` (não aceita
novos votos; apuração disponível).

As regras aceitas são `MAIORIA_SIMPLES` e `PERCENTUAL_MINIMO`. Para
`PERCENTUAL_MINIMO`, `percentualMinimo` deve ser maior que 0 e até 100; para
`MAIORIA_SIMPLES`, envie `null`. `quorumMinimo` fica entre 0 e 100, inclusive.
Além desses campos comuns, o cadastro recebe somente os campos aplicáveis ao
tipo do contexto:

- `ASSEMBLEIA`: `pauta` (texto) e `participantesAptos` (inteiro positivo).
- `CONDOMINIO`: `pesoVoto` (número positivo) e `unidadeApartamento` (texto).
- `CENTRO_ACADEMICO`: `categoriaSegmento` e `elegibilidade` (texto).

As respostas incluem `id`, `contextoNome`, `tipo`, `status` e os campos
específicos; os campos que não se aplicam são `null`. A apuração expõe
`resultado`, `quorumAtingido`, contagens/pesos de votos e
`percentualSimObtido`.

## Limitações conhecidas

- A versão do Node.js não está fixada por `.nvmrc` ou configuração equivalente.
- O PostgreSQL precisa ser instalado, criado e configurado manualmente.
- O arquivo `.env` precisa ser carregado/exportado manualmente antes da
  execução; o `setup.sh` apenas prepara dependências.
- O teste de contexto do backend pode exigir um PostgreSQL acessível com as
  variáveis configuradas.
