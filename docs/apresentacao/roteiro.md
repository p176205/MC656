# Roteiro — Projeto Final MC656

**Duração planejada:** 14min40s (máximo: 15min)  
**Versão examinada:** branch `fix/autorizacao-votacao`, commit `a434e9c`  
**Data da conferência:** 19/11/2026

> O deck mostra o estado real da branch conferida. Não considerar uma funcionalidade
> concluída só porque existe em outra branch, em uma issue ou no backend.

## Roteiro por slide

| Slide | Tempo | Apresentador(a) | Foco |
|---|---:|---|---|
| 1. Plataforma de participação e votação | 0:20 | Daniela | Apresentar equipe e produto |
| 2. Problema e proposta de valor | 0:50 | Daniela | Contextos diversos em uma plataforma |
| 3. Evolução, prioridades e escopo real | 1:00 | Daniela | Decisões de desenvolvimento e mudanças |
| 4. Requisitos convertidos em produto | 0:55 | Thiago | Rastreabilidade entre necessidade e entrega |
| 5. Arquitetura final | 1:05 | Thiago | REST, camadas e persistência |
| 6. Decisões técnicas e fronteiras de segurança | 0:55 | Thiago | Estratégias, sessão e ressalvas |
| 7. Organização do repositório | 1:00 | Gabriel | Responsabilidade de cada camada |
| 8. Testes e evidências de qualidade | 1:15 | Gabriel | Resultados atuais e históricos |
| 9. CI, execução e liberação | 1:25 | Priscilla | Pipeline e reprodutibilidade |
| 10. Demonstração I — acesso e contexto | 2:00 | Priscilla | Cadastro/login, comunidade |
| 11. Demonstração II — criação de votação | 2:15 | Ana Luiza | Deliberação ou eleição |
| 12. O que a interface não conclui ainda | 1:25 | Ana Luiza | Lacunas demonstradas sem simulação |
| 13. Fechamento: critérios C1–C5 | 0:15 | Ana Luiza | Evidências e conclusão |
| **Total** | **14:40** | **Todos** | **Dentro do limite de 15 minutos** |

### Slide 1 — Plataforma de participação e votação (0:20, Daniela)

**Fala sugerida:** “Somos Daniela, Thiago, Gabriel, Priscilla e Ana Luiza. Construímos
uma plataforma web para apoiar participação, comunicação e decisões em
assembleias, condomínios e Centros Acadêmicos. Vamos mostrar o produto, a
arquitetura, a qualidade verificada e também os limites que permanecem.”

**Evidência:** nomes conferidos em `README.md`; apresentação baseada no commit
`a434e9c`.

### Slide 2 — Problema e proposta de valor (0:50, Daniela)

**Fala sugerida:** “O problema que orienta o produto é a dispersão do processo
decisório: identificar o contexto, reunir participantes, comunicar a pauta e
registrar uma decisão. A proposta é concentrar esses passos em um único sistema.
O mesmo domínio cobre três tipos de contexto; as regras específicas não ficam
espalhadas pelas telas.”

**Cuidado:** apresentar isso como motivação do projeto, não como resultado de
pesquisa de usuários.

### Slide 3 — Evolução, prioridades e escopo real (1:00, Daniela)

**Fala sugerida:** “A evolução passou de cadastro e contextos para comunidade,
mensagens, criação de deliberações e eleições, testes e autorização de operações.
As tags A2, A4 e A5 documentam marcos de atividades, não uma release final deste
commit. O estado mostrado é a branch de autorização. A consulta de propostas em
painel, o registro de voto pela interface e o encerramento pela interface não
estão integrados nesta branch. Mantemos essa diferença explícita para não
confundir planejamento com entrega.”

**Evidência:** `git describe` resulta em `a4-v1.0.0-112-ga434e9c`; `a5-v1.0.0`
não é ancestral do commit apresentado.

### Slide 4 — Requisitos convertidos em produto (0:55, Thiago)

**Fala sugerida:** “O requisito de acesso vira cadastro, login e sessão. O de
organizar pessoas vira criação e entrada em contextos e troca de mensagens. O
de realizar decisões aparece no cadastro de votações por contexto, com
deliberação ou eleição, regras e quórum. A interface cadastra eleições e suas
chapas; já votar, encerrar e consultar propostas em tela continuam pontos
pendentes de integração visual.”

**Evidência:** `frontend/app`, `components/votacoes/VotacaoForm.tsx`,
`backend/src/main/java/com/MC656/backend/controller/VotacaoController.java`.

### Slide 5 — Arquitetura final (1:05, Thiago)

**Fala sugerida:** “O navegador executa Next.js, React e TypeScript. As páginas
usam a fachada de API, que concentra chamadas HTTP e JSON. No backend, os
controllers convertem rotas em operações, os services concentram regras, DTOs
definem o contrato e repositories usam Spring Data JPA para persistir no
PostgreSQL. A separação reduz o acoplamento entre apresentação, regras e
armazenamento.”

**Evidência:** código atual em `frontend/services/apiFacade.ts` e camadas em
`backend/src/main/java/com/MC656/backend/`.

### Slide 6 — Decisões técnicas e fronteiras de segurança (0:55, Thiago)

**Fala sugerida:** “Contexto é um tipo base com especializações, evitando
duplicar o fluxo para assembleia, condomínio e Centro Acadêmico. A criação do
formulário usa Strategy para diferenciar deliberação de eleição. O voto fica
separado do registro de participação para preservar a anonimização do conteúdo
do voto. Na branch apresentada, a identidade das operações protegidas vem da
sessão server-side, e a autorização verifica participação ou papel
administrativo. Isso não significa que toda a superfície esteja coberta: a
criação da votação ainda recebe `adminId` no DTO. Para produção, o cookie deve
usar `Secure` com HTTPS.”

**Evidência:** `Contexto`, `VotacaoForm.tsx`, `VotacaoService.java`,
`AuthenticatedUserService.java`, `VotacaoCreateDTO.java` e
`application.properties`.

### Slide 7 — Organização do repositório (1:00, Gabriel)

**Fala sugerida:** “No frontend, `app` organiza rotas, `components` contém
interfaces reutilizáveis, `services/apiFacade` isola a comunicação e `tests`
cobre funções e telas. No backend, controller, service, repository, DTO e
entity separam responsabilidades; `exception` padroniza respostas e `config`
guarda configurações. Na raiz, README e `setup.sh` documentam preparação; o
workflow executa a integração contínua.”

**Evidência:** árvore efetiva do repositório e `.github/workflows/ci.yml`.

### Slide 8 — Testes e evidências de qualidade (1:15, Gabriel)

**Fala sugerida:** “Na conferência atual, a suíte do backend passou com 189
testes e a do frontend com 18. Teste funcional observa entradas e resultados;
teste estrutural mede quais partes do código foram exercitadas. Cobertura
mostra execução, não prova ausência de defeitos. PIT altera o código de forma
controlada; um mutante morto significa que algum teste percebeu a alteração.
Um sobrevivente aponta uma possível lacuna, embora também possa ser equivalente.
Os números da Atividade 5 são históricos: 53 testes funcionais específicos,
118 no backend, 100% somente nos escopos medidos de `ContextoController` e
`MensagemService`, 270/344 mutantes mortos e Test Strength 95%, com um mutante
sobrevivente em `VotacaoService`. Não há relatório novo de JaCoCo/PIT para o
commit atual; portanto não atualizamos essas métricas.”

**Evidência atual:** relatórios Surefire da execução integral em 19/11/2026.
**Evidência histórica:** relatório da Atividade 5; cobertura não representa o
backend inteiro.

### Slide 9 — CI, execução e liberação (1:25, Priscilla)

**Fala sugerida:** “O workflow no GitHub Actions está configurado para push e
pull request em `main` e `develop`: compila e testa o backend, gera artefato de
cobertura, instala dependências com `npm ci`, executa testes e lint e constrói o
frontend. Isso descreve o pipeline, não afirma que este branch teve um run
verde. A reprodução local exige Java 21, Node, npm e PostgreSQL configurado
manualmente. Não há Docker Compose neste repositório. As tags A2/A4/A5 são
marcos; o commit mostrado não é uma release final etiquetada.”

**Comandos demonstrados no README:**

```sh
cp .env.example .env
# preencher DB_URL, DB_USERNAME, DB_PASSWORD e NEXT_PUBLIC_API_URL
sh setup.sh

# terminal 1, depois de exportar as variáveis do .env
cd backend && ./mvnw spring-boot:run

# terminal 2
cd frontend && npm run dev
```

**Evidência:** `README.md`, `setup.sh`, `.github/workflows/ci.yml`, ausência de
arquivo Compose; `git describe` na branch apresentada.

### Slide 10 — Demonstração I: acesso e contexto (2:00, Priscilla)

**Fala sugerida e ações:** abrir as telas reais de cadastro/login; autenticar
uma conta de demonstração preparada; mostrar a home, o contexto selecionado e a
comunidade; se o ambiente e as contas estiverem preparados, demonstrar criação
ou entrada em contexto e mensagens. Não expor credenciais reais no projetor.

**Capturas incorporadas:** `capturas/tela-login.png` e
`capturas/tela-cadastro.png`, capturadas do frontend local em 19/11/2026.
**Pré-condição:** backend ativo e base PostgreSQL de demonstração populada ou
contas criadas antes da apresentação. Se não estiver, apresentar somente as
duas telas reais e explicar a dependência, sem simular a autenticação.

### Slide 11 — Demonstração II: criação de votação (2:15, Ana Luiza)

**Fala sugerida e ações:** navegar do contexto até “Nova votação”; apresentar
os dois tipos e, se os dados de demonstração estiverem disponíveis, cadastrar
uma deliberação ou eleição. Na eleição, mostrar o formulário de chapa,
proposta textual e seleção de membros. Conferir título, datas, regra e quórum.
Usar uma votação de demonstração, nunca criar/alterar dados de produção.

**Pré-condição:** conta com contexto e permissão para criar votação. A tela de
criação existe em `components/votacoes/VotacaoForm.tsx`; não apresentar o
formulário como evidência de que o voto já pode ser registrado pela interface.

### Slide 12 — O que a interface não conclui ainda (1:25, Ana Luiza)

**Fala sugerida:** “O backend tem as operações de voto, encerramento, consulta
de chapas e apuração, e a branch acrescenta autenticação e autorização para
voto, encerramento e consulta de chapas. No frontend desta versão, não há fluxo
de registro do voto nem botão de encerramento; o painel PF-5 para consultar
propostas também não está integrado. A apuração é exibida nos detalhes quando
a votação já está encerrada. A autorização da criação da votação ainda usa
`adminId` informado no payload. São próximos passos, não funcionalidades que
vamos fingir demonstrar.”

**Demonstração de contingência:** mostrar os testes backend que exercitam
autorização, sem afirmar que isso substitui um fluxo de usuário completo.

### Slide 13 — Fechamento: critérios C1–C5 (0:15, Ana Luiza)

**Fala sugerida:** “C1: fluxos web demonstrados e lacunas declaradas. C2:
requisitos ligados a camadas e decisões. C3: testes atuais, pipeline e métricas
históricas separadas. C4: README e preparação repetível, com PostgreSQL manual
e sem Compose. C5: equipe completa e apresentação dentro dos 15 minutos.
Obrigada.”

## Lista de capturas de tela

### Capturas reais já incorporadas

- `capturas/tela-login.png` — login em `http://127.0.0.1:3000/`.
- `capturas/tela-cadastro.png` — cadastro em `http://127.0.0.1:3000/cadastro`.

Ambas foram capturadas do frontend local real, em 1000 × 680, com campos vazios.
São telas
visuais reais, não comprovam que o backend ou uma conta específica estejam
disponíveis.

### Capturas a preparar no ambiente de demonstração

- Home após login com um contexto selecionado.
- Comunidade/mensagens dentro de um contexto de demonstração.
- Formulário real de nova deliberação.
- Formulário real de nova eleição com chapa e membros.
- Detalhes e apuração de uma votação já encerrada, se os dados preparados
permitirem acessar a tela.

Não criar captura de “registro de voto”, “encerramento” ou “consulta de
propostas” como tela funcional: esses fluxos não estão integrados no frontend
do commit apresentado. Não usar dados pessoais ou credenciais reais.

## Checklist da demonstração

- [ ] Usar o commit/branch indicado no início do roteiro.
- [ ] Confirmar PostgreSQL acessível e `.env` local preenchido (não projetar o arquivo).
- [ ] Executar `sh setup.sh` somente se as dependências ainda não estiverem instaladas.
- [ ] Exportar as variáveis locais e iniciar `./mvnw spring-boot:run`.
- [ ] Iniciar `npm run dev` com `NEXT_PUBLIC_API_URL=http://localhost:8080`.
- [ ] Confirmar cadastro/login e manter duas contas de demonstração não sensíveis.
- [ ] Preparar um contexto e uma conta com papel administrativo.
- [ ] Preparar, se for demonstrar entrada, o código do contexto e a segunda conta.
- [ ] Preparar dados separados para uma deliberação e uma eleição/chapa.
- [ ] Ensaiar a navegação até a página de detalhes e, se possível, uma apuração encerrada.
- [ ] Cronometrar os slides 10–12; abortar tentativas de rede que ultrapassem o tempo.
- [ ] Se voto/encerramento/painel de propostas forem solicitados, mostrar o limite
  claramente e recorrer aos testes, sem fabricar telas ou respostas.
- [ ] Ter as capturas reais incorporadas como contingência.

## Checklist de critérios de avaliação

| Critério | Peso | Evidência apresentada |
|---|---:|---|
| C1 — Produto final e funcionalidades | 30% | Slides 2, 4 e 10–12: telas reais, fluxos disponíveis e lacunas explícitas |
| C2 — Requisitos, arquitetura e implementação | 25% | Slides 4–7: rastreabilidade, diagrama de camadas e decisões ligadas ao código |
| C3 — Qualidade, testes, CI e liberação | 20% | Slides 8–9: 189 testes backend e 18 frontend atuais; métricas A5 históricas; pipeline configurado |
| C4 — Organização e reprodutibilidade | 15% | Slides 7 e 9: estrutura, README, `setup.sh`, PostgreSQL manual e ausência de Compose |
| C5 — Apresentação final | 10% | Slides 1 e 13: cinco integrantes identificados, fala distribuída e 14min40s |

## Conferência do estado do produto

- A branch examinada é `fix/autorizacao-votacao`, commit `a434e9c`; o worktree
  estava limpo antes da produção dos materiais.
- A execução atual do backend terminou com 189 testes, 0 falhas, 0 erros e 0
  ignorados. A execução atual do frontend terminou com 18 testes em 3 arquivos.
- A Atividade 5 documenta 118 testes e métricas de JaCoCo/PITest. Elas são
  históricas; não houve relatório novo de cobertura ou mutação neste commit.
- O workflow CI está definido para `main` e `develop`; não foi verificado um run
  do Actions para o commit/branch da apresentação.
- `a5-v1.0.0` não é ancestral do commit atual. Não declarar este commit uma
  release final.
- PostgreSQL precisa ser instalado/configurado pelo usuário. Não existe
  `docker-compose.yml`/`compose.yaml` no repositório.
- A branch não inclui painel de consulta de propostas PF-5, submissão de voto
  pela interface nem botão de encerramento.
- A criação da votação ainda autoriza com `adminId` do DTO; não foi corrigida
  pela PF-6 implementada neste commit.
