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

Sobe o Postgres em `localhost:5435` (`nuvexa`/`nuvexa`).

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

### Profile `local`

O `application.yml` base já tem defaults de `localhost` pra tudo (banco, CORS, Swagger habilitado),
então rodar o backend sem nenhum profile ativo já funciona para o dia a dia local — o profile
`local` só formaliza isso e liga logging mais verboso (`com.nuvexa: DEBUG`) sem precisar exportar
variável nenhuma:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Ambiente de desenvolvimento remoto (profile `dev`)

Profile Spring dedicado a subir o backend em um servidor remoto de dev/testes (container Docker),
não o dia a dia local nem produção. Ativação:

```bash
java -jar app.jar -Dspring.profiles.active=dev
# ou
SPRING_PROFILES_ACTIVE=dev java -jar app.jar
```

Variáveis de ambiente:

| Variável | Obrigatória no profile `dev`? | Default (herdado de `application.yml`) |
| --- | --- | --- |
| `DB_HOST` | Não | `localhost` |
| `DB_PORT` | Não | `5435` |
| `DB_NAME` | Não | `nuvexa` |
| `DB_USER` | Não | `nuvexa` |
| `DB_PASSWORD` | Não | `nuvexa` |
| `SERVER_PORT` | Não | `8080` |
| `API_BASE_URL` | Não | `http://api.nuvexa.dev:8080` |
| `FRONTEND_URL` | **Sim** — sem essa variável a aplicação falha ao subir | — |
| `JWT_SECRET`, `MAIL_*`, `STORAGE_*` | Não (mas troque os defaults antes de expor o ambiente) | ver `application.yml` |

`FRONTEND_URL` é obrigatória no profile `dev` (sem fallback para `localhost:5173`, que não faz
sentido em um ambiente remoto) — deve apontar para o host de dev definido para o front-end. O
banco de dados assume, por padrão, um Postgres via `docker compose` rodando no próprio servidor
remoto (mesmo padrão do `docker-compose.yml` da raiz); ajuste `DB_HOST`/`DB_PORT` se o container
do backend não estiver na mesma rede Docker do banco. O Swagger UI (`/swagger-ui.html`) e o spec
OpenAPI (`/v3/api-docs`) ficam explicitamente habilitados nesse profile.

#### Domínio do ambiente de dev remoto

Padrão de nomenclatura adotado (documentado aqui para ser seguido quando surgirem outros
ambientes): `<camada>.nuvexa.<ambiente>`. Hoje só existe uma camada (`api`) e um ambiente remoto
(`dev`):

- **`api.nuvexa.dev`** — backend, ambiente de dev remoto. Ainda sem proxy reverso, então a porta
  do container vai explícita na URL: `http://api.nuvexa.dev:8080`.
- Quando existir produção, o mesmo backend vira `api.nuvexa.com` (ou domínio próprio da vertical,
  ex. `nutri.<domínio>`) — sem o sufixo de ambiente. Staging seguiria `api.nuvexa.staging` (ou
  equivalente), mesma lógica.

`api.nuvexa.dev` não é um domínio público — resolva-o localmente apontando para o IP do servidor
remoto, adicionando ao `/etc/hosts` (Linux/Mac: `/etc/hosts`; Windows:
`C:\Windows\System32\drivers\etc\hosts`) em cada máquina que for acessar o Swagger:

```
<IP-do-servidor-remoto>  api.nuvexa.dev
```

(ou registre isso em DNS interno, se/quando houver um). O front-end continua rodando local
(`npm run dev`, `localhost:5173`) — para apontá-lo para o backend remoto em vez do local, crie
`frontend/.env.development.local` (já coberto pelo `.gitignore`, não é commitado) com:

```
VITE_API_BASE_URL=http://api.nuvexa.dev:8080/api/v1
```

e ajuste `FRONTEND_URL=http://localhost:5173` nas variáveis de ambiente do backend remoto (profile
`dev`) para liberar o CORS dessa origem.

## Testes

```bash
cd backend
./mvnw test
```

Testes de repositório/integração usam o Postgres real (não há fallback em memória) — suba o `docker compose` antes.

## Arquitetura

O backend segue *package by feature* com verticais de negócio plugáveis (`core/` + `verticals/`, hoje só `nutricao`). Veja `CLAUDE.md` para detalhes de arquitetura, convenções e domínio.
