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
npm run lint
npm run build
npm run start
```

Não há script de testes automatizados definido no `frontend/package.json`.

## Limitações conhecidas

- A versão do Node.js não está fixada por `.nvmrc` ou configuração equivalente.
- O PostgreSQL precisa ser instalado, criado e configurado manualmente.
- O arquivo `.env` precisa ser carregado/exportado manualmente antes da
  execução; o `setup.sh` apenas prepara dependências.
- O teste de contexto do backend pode exigir um PostgreSQL acessível com as
  variáveis configuradas.
