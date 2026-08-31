## REGRA DE ECONOMIA DE TOKENS

**Princípio:** use sempre a menor operação capaz de resolver a tarefa.

### Leitura

* Não leia arquivos grandes integralmente sem necessidade.
* Para arquivos >150 linhas, busque primeiro o termo, classe, método ou componente relevante.
* Prefira `busca → leitura pontual` a leitura completa.
* Leia somente as linhas necessárias.
* Não releia conteúdo já disponível no contexto.

### Busca

* Comece com buscas restritas.
* Amplie o escopo somente se necessário.
* Prefira filtros por pasta, arquivo, extensão ou domínio.
* Para múltiplas ocorrências, obtenha primeiro localização/quantidade e leia apenas os trechos necessários.
* Não repita buscas que já produziram a informação necessária.

### Edição

* Faça alterações pontuais.
* Nunca reescreva um arquivo inteiro para alterar poucos trechos.
* Agrupe alterações relacionadas quando possível.
* Não altere arquivos que não sejam necessários para a tarefa.

### Comandos

* Não execute comandos apenas para confirmar informações já disponíveis no contexto.
* Prefira comandos específicos ao escopo necessário.
* Não execute validações completas quando uma validação mais específica for suficiente.
* Evite executar o mesmo comando repetidamente sem necessidade.
* Reutilize resultados de comandos já executados quando ainda forem válidos.

### Contexto

* Reutilize informações já presentes no contexto.
* Não repita código, arquivos, requisitos ou explicações já disponíveis.
* Não faça resumos periódicos por quantidade de mensagens.
* Ao continuar uma tarefa, use as decisões já tomadas em vez de redescobri-las.

### Respostas

* Seja direto.
* Evite preâmbulos e conclusões desnecessárias.
* Prefira listas e respostas compactas.
* Máximo de 3 frases por ponto, salvo necessidade.
* Não explique o que já está evidente no contexto.
* Não reproduza arquivos ou códigos completos quando apenas um trecho for necessário.

### Regra principal

**Antes de cada operação, escolha a menor leitura, busca, edição, comando ou resposta que resolva a tarefa.**


This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Nuvexa is a SaaS for nutrition professionals to manage patients. Monorepo with two independent apps:
- `backend/` — Java 25 / Spring Boot 4 REST API (Maven).
- `frontend/` — Vue 3 + TypeScript SPA (Vite).

They are not wired together by tooling (no root build script) — run/build each independently.

## Commands

### Backend (`backend/`)
- `docker compose up -d` (from repo root) — starts the Postgres 16 dev DB (`nuvexa`/`nuvexa`, host port 5435, mapped to the container's 5432 — check `docker-compose.yml`/`application.yml`'s `DB_PORT` default before assuming 5432). Must be running before `./mvnw` targets that touch the DB (the app uses `ddl-auto: validate` — schema comes only from Flyway migrations, there's no embedded/H2 fallback).
- `./mvnw spring-boot:run` — run the API on port 8080 (override via `SERVER_PORT`).
- `./mvnw test` — run all tests (JUnit 5 + Mockito). Repository/integration tests (`*RepositoryTest`, `*IntegrationTest`) hit the real Postgres DB above, not an in-memory one.
- `./mvnw test -Dtest=ClassName#methodName` — run a single test method; `-Dtest=ClassName` for a whole class.
- `./mvnw compile` — compile only (also regenerates QueryDSL Q-classes under `target/generated-sources`).
- `./gerar_migration <name>` (from `backend/`) — scaffolds a new empty Flyway migration file at `src/main/resources/db/migration/V<timestamp>__<name>.sql` (sanitizes the name to `snake_case`). Migrations are applied automatically on startup (`spring.flyway.enabled: true`).
- Key env vars (all optional, see `application.yml`): `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD`, `SERVER_PORT`, `FRONTEND_URL` (CORS allowed origin, defaults to `http://localhost:5173`), `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`.

### Frontend (`frontend/`)
- `npm run dev` — Vite dev server. **Must run on port 5173** (the backend's CORS config only whitelists that origin by default) — if something else is already listening there, reuse it rather than starting on another port; requests silently fail (503/403) from any other port.
- `npm run build` — production build (esbuild/Vite). This is the reliable way to catch real compile errors.
- `npm run type-check` (`vue-tsc -b`) — currently broken project-wide (reports "module cannot have multiple default exports" on almost every `.vue` file, including untouched ones like `App.vue`), most likely `vue-tsc`/Volar not fully supporting `vue-facing-decorator` class components. Don't trust its output as a signal; prefer `npm run build`.
- No lint script/config exists yet.
- `npm run preview` — preview a production build.

## Architecture

### Backend: layered by package, package by feature

```
com.nuvexa
├── platform/       cross-cutting infra: security (JWT), global exception handling, ModelMapper/QueryDSL config, MessageSource
├── core/
│   ├── identity/    User, Role (ADMIN, PROFESSIONAL) — auth
│   ├── patient/     PatientCore — the platform-wide identity of a patient (name, birthDate, gender)
│   └── organization/  Organization/OrganizationType/OrganizationStatus — scaffolded, not wired to anything yet (see below)
└── verticals/
    └── nutrition/   NutritionProfile (1:1 with PatientCore: height/weight/goal/activityLevel/calories/notes), calculators, controller/service/DTOs
```

(Package/class names above are the pre-translation English originals — the actual code on disk uses the Portuguese domain names described under "Translation" below, e.g. `core.paciente`, `verticals.nutricao`, `PerfilNutricional`.)

### Verticals (Strategy + Registry)

`core.vertical` holds the cross-vertical contract: `Especialidade` (enum, one value per vertical — currently only `NUTRICAO`), `VerticalStrategy` (interface a vertical implements to describe itself), `DescritorDeVertical` (record: `especialidade`/`nome`/`rotaBase`), and `VerticalRegistry` (`@Component` that collects every `VerticalStrategy` bean via constructor injection of `List<VerticalStrategy>`, keyed by `Especialidade`; throws `IllegalStateException` at startup if two beans declare the same `Especialidade`). `verticals.nutricao.NutricaoStrategy` is the first (and currently only) implementation.

Rules to preserve when adding a second vertical or touching this layer:
1. `VerticalRegistry` never executes business logic — it only discovers/looks up/lists strategies.
2. `VerticalStrategy` only describes a vertical (`especialidade()` + `descrever()`); it never calls a `Repository`, replaces a `Service`/`Controller`, or centralizes business rules — those stay in the vertical's own `controller/service/repository`.
3. Shared modules (e.g. the report layer, see below) must never depend on a specific vertical — dependency direction is always `verticals.* → módulo compartilhado`, never the reverse.
4. `DescritorDeVertical` metadata stays small (identity/labels), never a full screen/layout/workflow description.
5. Don't create a new shared module (`modulos.*`) until a second real consumer needs it.
6. Don't add architectural patterns (Dispatcher, Factory, Chain of Responsibility, plugin system, schema-driven UI, etc.) to this layer without a concrete, current need — Spring MVC already does the HTTP dispatch, `VerticalRegistry` already does the lookup.

**Report generation is an external dependency, not backend code.** `RelatorioColuna`/`RelatorioColunaDTO`/`RelatorioResponseDTO`/`RelatorioExcelWriter` used to live in `platform.report` — they were extracted to a separate Maven artifact, `com.nuvexa:nuvexa-relatorios` (repo `~/workspace-mago/java/nuvexa-relatorios`, package `com.nuvexa.relatorios`, installed to the local `~/.m2`), declared as a normal dependency in `backend/pom.xml`. It's framework-agnostic (Lombok + Apache POI only, no Spring). The Nutrição-specific column definitions still live in the backend, as static methods on `verticals.nutricao.report.dto.response.PacienteRelatorioLinhaDTO` (`colunas(...)`, `from(...)`) — that's correct, it's the vertical-specific part.

Each feature package follows `controller → service → repository`, with request/response DTOs kept separate from JPA entities. Entities extend `platform.persistence.AbstractModel` (auto `id`/`createdAt`/`updatedAt`). There is no `*Mapper` layer — conversion between entities and DTOs is a method on the Model or DTO itself (`request.toEntity()`, `request.atualizar(entidade)`, `ResponseDTO.from(entidade)`, `DTO.of(params...)`), never in Controller/Service (there's a `ModelMapper` bean configured but it isn't used — see the nuvexa-backend skill's "Conversão (Model/DTO)" section for the full convention and its dependency-direction constraint). QueryDSL is used for dynamic search queries (Q-classes are generated at compile time via annotation processing — if they look missing/stale, `./mvnw compile`).

Errors: throw `NegocioException(HttpStatus, message)` from services; `GlobalExceptionHandlerController` (`@RestControllerAdvice`) converts it (and validation/type-mismatch/access-denied/etc. exceptions) into a uniform `ApiErro` JSON body. User-facing messages are resolved through `MessageSource` against `mensagens/messages.properties`, hardcoded to `pt-BR` locale — there's no i18n on the backend side, all API error messages are Portuguese.

Auth: stateless JWT (`jjwt`), `JwtAuthenticationFilter` populates the Spring Security context from the `Authorization: Bearer` header on every request. `SecurityConfig` only draws the public/authenticated boundary (`/api/v1/auth/**` public, everything else authenticated); resource-level authorization lives in `@PreAuthorize` on the method (`@EnableMethodSecurity` is on), e.g. `DELETE /api/v1/pacientes/{id}` requires `ROLE_ADMIN`. Password-reset issues a token but there's no real email sending yet — the reset link is just logged (`AutenticacaoService`).

### Identidade, organização e escopo

Six separate concepts — do not collapse them:

```
Usuario
  |
  +--- Perfil            papel de acesso à plataforma (ADMIN | PROFISSIONAL)
  |
  +--- Vinculo           papel dentro da organização (PROPRIETARIO | GESTOR | MEMBRO)
          |
          +--- Organizacao
                  |
                  +--- Paciente          (FK organizacao_id, NOT NULL)
                          |
                          +--- PerfilNutricional   (1:1, vertical Nutrição)

Especialidade / VerticalStrategy   vertical de negócio (NUTRICAO) — independente dos papéis acima
```

- `Vinculo` (`core.vinculo`) é entidade de associação `Usuario`×`Organizacao` com `papel` e `ativo`, `UNIQUE(usuario_id, organizacao_id)` — um usuário pode pertencer a várias organizações.
- `ContextoDeAutenticacao` (`core.contexto`) é o **único** ponto que lê o `SecurityContextHolder`: `usuarioAtual()`, `vinculoAtual()`, `organizacaoAtual()`, `organizacaoAtualId()`. Nenhum service deve falar com o `SecurityContextHolder` direto. Usuário sem vínculo ativo → 403. Hoje `vinculoAtual()` devolve o vínculo ativo mais antigo; a troca de organização (quando existir) muda só esse método.
- **Escopo de paciente é aplicado na query**, nunca filtrando em Java: `PacienteService` e `PacienteRelatorioService` sempre incluem `paciente.organizacao.id.eq(organizacaoAtualId())`, e a busca por id usa `findByPacienteIdAndPacienteOrganizacaoId`. Paciente de outra organização retorna **404, não 403** (um 403 confirmaria que aquele id existe).
- `GET /api/v1/contexto` devolve usuário + organização + papel para o frontend decidir o que exibir — a decisão de segurança continua sendo do backend.
- Cadastro (`POST /api/v1/auth/cadastrar`) provisiona automaticamente uma `Organizacao` do tipo `INDIVIDUAL` e um `Vinculo` `PROPRIETARIO` para o novo usuário; sem isso ele autenticaria mas ficaria sem escopo nenhum. Entrar em organização existente (convite) ainda não existe.

`AUDITORIA_CORE_SAAS.md` e `AUTENTICACAO_E_AUTORIZACAO.md` eram os docs de análise que propuseram/descreveram esse desenho originalmente — **ambos foram removidos do repositório** (não recriar sem necessidade real); o resumo aqui é o que resta como referência. Da proposta original, foram implementados `Organizacao` e `Vinculo` (no lugar de `Membership`) — `Professional`, `ProfessionalSpecialty` e `PatientRelationship` **não** existem, e paciente pertence direto à organização, sem profissional responsável.

### Frontend: feature folders + class-style components

Vue 3 with `vue-facing-decorator` (class-based `@Component`/`@Prop`/`@Emit`/`@VModel` components, not `<script setup>`), Vuetify 4, Pinia, vue-router 4, vue-i18n (Portuguese only, `src/translations/pt-BR.ts`).

Pages live under `src/pages/<domain>/`, each owning its own `components/` subfolder for components used *only* by that page (e.g. `pages/patients/components/PatientTable.vue`). A component only gets promoted to the shared `src/components/common/` folder (prefixed `Nuvexa*`, e.g. `NuvexaSelect`, `NuvexaFiltersCard`, `NuvexaSummaryCards`) once it's actually reused by a second page domain — don't add new shared components speculatively. Routes are split by domain into `src/router/*.routes.ts` and merged in `src/router/index.ts`; the global nav guard there redirects unauthenticated users to `/login`.

All main screens are real and backend-connected — there is no mock data left anywhere in `frontend/src` (`src/mocks/` is empty):
- `/pacientes`, `/pacientes/:id`, `/pacientes/novo`, `/pacientes/:id/editar` (`nutricao/pages/PacienteLista.vue`, `PacienteDetalhe.vue`, `PacienteFormulario.vue`) via `nutricao/services/paciente-service.ts`.
- `/consultas` and its create/edit/view routes (`pages/consultas/ConsultaLista.vue`, `ConsultaFormulario.vue`) via `service/consulta-service.ts`.
- `/agenda` (`pages/agenda/Agenda.vue`, Vuetify's native `v-calendar`) reuses the same `consultaService.listar()` as Consultas.
- `/` (Home/Dashboard, `pages/dashboard/Dashboard.vue`) aggregates `GET /pacientes`, `/consultas`, `/pacientes/relatorio` and `/contexto` client-side — see `pages/dashboard/components/` for how each card/chart is computed.

All form inputs (`VTextField`/`VSelect`/`VAutocomplete`/`VCombobox`/`VTextarea`) default to `variant: 'underlined'` globally via `defaults` in `plugins/vuetify.ts` — don't set `variant` per-field, that's the app-wide standard.

`EmConstrucao.vue` is a generic "coming soon" placeholder shared by the not-yet-built routes (nutrição/avaliações/configurações — `/relatorios` is real now, not a placeholder) — it isn't owned by one page domain, so it stays directly under `src/pages/`.
