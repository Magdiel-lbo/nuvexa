# Nuvexa

SaaS para profissionais de nutrição gerenciarem pacientes.

Monorepo com dois apps independentes (sem build/tooling compartilhado — rode/faça build de cada um separadamente):

- **`backend/`** — API REST em Java 25 / Spring Boot 4 (Maven)
- **`frontend/`** — SPA em Vue 3 + TypeScript (Vite, Vuetify, Pinia)

## Stack

**Backend**
- Java 25, Spring Boot 4, Maven
- PostgreSQL 16 (Flyway para migrations, `ddl-auto: validate`)
- Spring Security com JWT (`jjwt`)
- QueryDSL para queries dinâmicas

**Frontend**
- Vue 3 (`vue-facing-decorator`, componentes class-based)
- Vuetify 4, Pinia, Vue Router 4, Vue I18n

## Como rodar

### Banco de dados

```bash
docker compose up -d
```

Sobe o Postgres em `localhost:5432` (`nuvexa`/`nuvexa`).

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

API disponível em `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App disponível em `http://localhost:5173` (porta obrigatória — o CORS do backend só libera essa origem por padrão).

## Testes

```bash
cd backend
./mvnw test
```

Testes de repositório/integração usam o Postgres real (não há fallback em memória) — suba o `docker compose` antes.

## Arquitetura

O backend segue *package by feature* com verticais de negócio plugáveis (`core/` + `verticals/`, hoje só `nutricao`). Veja `CLAUDE.md` para detalhes de arquitetura, convenções e domínio.
