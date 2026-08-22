---
name: nuvexa-frontend
description: Padrões de implementação do frontend Nuvexa (frontend/) — Vue 3, TypeScript, vue-facing-decorator, Vuetify 4, Pinia, vue-router, vue-i18n, dashboards, filtros, gráficos, responsividade e mocks. Use ao criar ou alterar qualquer página, componente, store, rota ou tradução dentro de frontend/src.
---

# Nuvexa Frontend

Regras de execução para tarefas de frontend. Arquitetura geral (estrutura de pastas por domínio, quais telas são mock vs reais, CORS/porta 5173, `npm run build` vs `type-check`) já está no `CLAUDE.md` da raiz — não repita isso aqui, só aplique.

## Vue 3 + TypeScript + vue-facing-decorator

Todo componente é **class-based**, nunca `<script setup>`:

```vue
<script lang="ts">
import { Component, Prop, Vue, Emit, Watch, VModel } from 'vue-facing-decorator'

@Component({ name: 'MeuComponente', components: { OutroComponente }, emits: ['update:modelValue', 'clear'] })
export default class MeuComponente extends Vue {
  @Prop({ required: true }) titulo!: string
  @Prop({ default: null }) valor!: string | null
  @VModel({ required: true }) model!: AlgumTipo   // quando o componente é um form/campo controlado

  get algumaCoisaComputada() { ... }

  @Watch('titulo')
  onTituloChange(novo: string) { ... }

  @Emit('clear')
  limpar() {}
}
</script>
```

- `@Component` sempre declara `name` (bate com o nome do arquivo) e, se emitir eventos, a lista `emits`.
- Acesso a store dentro do componente é sempre via getter, nunca chamando `useXStore()` direto no template:
  ```ts
  get appStore() { return useAppStore() }
  ```
- Textos de UI nunca são hardcoded — sempre `this.$t('namespace.chave')` (ou `$t(...)` no template). A única exceção tolerada hoje são mensagens de toast de ações ainda não integradas ("layout de demonstração"), que são literais — não generalize esse hábito para texto real de UI.

## Vuetify 4

- Não defina `variant` em inputs manualmente — o padrão global (`underlined`) já vem de `plugins/vuetify.ts`.
- Superfícies de card seguem sempre `variant="flat" color="surface-variant"` com `border-radius: 12px` no scss do componente. Não use `elevation`/sombra.
- Tabelas usam `v-data-table` com slots nomeados (`#item.<key>="{ item }"`) para customizar células, e `mobile-breakpoint="sm"` para virar cartões em telas pequenas.
- Ícones sempre `mdi-*` (Material Design Icons, já incluso).
- Diálogos (`v-dialog`) seguem o padrão de `PatientCreateDialog.vue`: prop `modelValue` + emit `update:modelValue`, form dentro reseta via `@Watch('modelValue')` quando abre.
- Feedback de ação (sucesso/erro/demo) sempre via `appStore.setToast({ mensagem, erro })`, nunca `alert()`/`console.log` visível ao usuário.

## Pinia

Stores usam a Options API do Pinia (`state`/`getters`/`actions`), não a `setup store` syntax — siga `store/app.store.ts` ou `store/dashboard-filters.store.ts` como referência. Uma store só existe se mais de uma parte da árvore de componentes precisa do estado; estado local de uma página fica na própria classe do componente.

## vue-router

Cada domínio tem seu próprio `router/<dominio>.routes.ts` exportando um array de `RouteRecordRaw`, importado e espalhado (`...xRoutes`) em `router/index.ts`. Toda rota tem `meta.title` (usado no `<v-app-bar>` e na sidebar); use `meta.description` quando a rota também alimenta algum resumo de página. Nunca importe componente de página de forma estática — sempre `component: () => import('../pages/...')`.

## vue-i18n

Só existe `pt-BR` (`translations/pt-BR.ts`). Chaves são agrupadas por domínio/tela (`paciente.*`, `dashboardPacientes.*`, `dashboardConsultas.*`, `consulta.*`, `agenda.*`, `menu.*`, `acao.*`, `erro.*`, `sucesso.*`, `validacao.*`). Ao adicionar uma tela nova, crie um novo bloco de nível superior nomeado pela tela (`dashboardX`) em vez de espalhar chaves soltas, e reaproveite os blocos genéricos já existentes (`acao`, `erro`, `sucesso`, `filtroComum`) em vez de duplicar "Salvar"/"Cancelar"/"Excluir" etc.

## Organização de páginas e reuso de componentes

Ao criar uma tela nova: crie `pages/<dominio>/NomeDaTela.vue` e, se ela precisar de subcomponentes, `pages/<dominio>/components/`. Um componente só sobe para `components/common/` (prefixo `Nuvexa*`) quando **já está sendo reusado por um segundo domínio** — nunca crie um componente direto em `common/` de forma especulativa. Antes de escrever um componente novo, cheque `components/common/` (`NuvexaSelect`, `NuvexaFiltersCard`, `NuvexaSummaryCards`, `NuvexaEmptyState`, `NuvexaDateField`, `NuvexaStatusFilter`, `NuvexaPeriodFilter`, `NuvexaYesNoFilter`) — a resposta correta costuma ser compor com o que já existe, não reescrever.

## Dashboards

Toda tela de "dashboard de listagem" (ex.: `PatientsDashboard.vue`, `ConsultasDashboard.vue`) segue o mesmo esqueleto — replique-o em vez de inventar um novo layout:

1. Header: `<h1>` + subtítulo + botão de ação primária (`v-btn color="primary" prepend-icon="mdi-plus"`).
2. `<NuvexaSummaryCards :cards="summaryCards" />` — `summaryCards` é um getter que mapeia contagens para `SummaryCardItem[]` (`label/value/icon/color`).
3. `<XFilters ... @clear="clearFilters" />` — ver seção Filtros.
4. `v-card` envolvendo `<XTable v-if="filtrados.length > 0">` **ou** `<NuvexaEmptyState v-else>` (nunca tabela vazia sem estado vazio dedicado).
5. Estado do componente: campos de filtro como data própria, um getter `hasActiveFilters`, um getter `filtrados` que aplica os filtros sobre a lista bruta, e um método `clearFilters()`.
6. Ações ainda não integradas ao backend real disparam toast de demonstração (`appStore.setToast(...)`); ações que já mexem em dado real (mock ou API) fazem a mutação de verdade — não finja as duas coisas ao mesmo tempo na mesma ação.

## Filtros

Sempre envolva os campos de filtro em `<NuvexaFiltersCard :show-clear="hasActiveFilters" :clear-label="$t(...) as string" @clear="...">`. Dentro dela: reuse `NuvexaStatusFilter`/`NuvexaPeriodFilter`/`NuvexaYesNoFilter`/`NuvexaDateField` quando o campo já existe nesse formato; caso contrário use `NuvexaSelect` genérico passando `items` (com opção "Todos" no topo, `value: null`). Todo filtro segue o contrato `:model-value` + `@update:model-value` (nunca `v-model` direto no wrapper — quem centraliza o estado é a página/dashboard, os filtros são controlados).

## Gráficos

Gráficos usam `vue-chartjs` (`Line`/`Bar`) envolvidos num `v-card variant="flat" color="surface-variant"` com título próprio (ver `DashboardLineChart.vue`/`DashboardBarChart.vue`). Regras:

- Cores **nunca** são hex hardcoded — sempre `getChartTheme()` de `util/chart-theme.ts`, que lê os tokens vivos do tema Vuetify atual.
- Todo componente de gráfico usa `:key="themeStore.name"` no elemento do chart.js, para forçar remount ao trocar de tema claro/escuro (chart.js não re-renderiza cores sozinho).
- `responsive: true` + `maintainAspectRatio: false` sempre, com o canvas dentro de um container de altura fixa (`.chart-card__canvas { height: 260px; position: relative; }`).

## Responsividade

- Grids de cards usam CSS Grid com breakpoints em `@media (max-width: 960px)` (reduz colunas, geralmente pra 2) e `@media (max-width: 600px)` (1 coluna) — siga esses dois breakpoints, não invente outros.
- Headers de página (título + ação) usam `display:flex; flex-wrap:wrap; justify-content:space-between; gap:16px` para empilhar naturalmente em telas estreitas, sem precisar de media query própria.
- `v-data-table` sempre com `mobile-breakpoint="sm"`.

## Mocks

Quando a tela não tem endpoint real ainda (ou a tarefa pede explicitamente "mock primeiro"): tipos em `types/<dominio>.ts`, gerador de dados em `mocks/<dominio>.mock.ts`. Mesmo sendo 100% mock, crie um `service/<dominio>-service.ts` fino (como `dashboard-service.ts`) chamando o mock por baixo — assim trocar para API real depois é editar um arquivo só, não a página inteira. Nunca misture item vindo de API real com item mockado na mesma lista de uma página.

## Validação

Depois de qualquer mudança de frontend, rode `npm run build` (dentro de `frontend/`) e trate build limpo como critério de aceite — `npm run type-check` não é confiável neste projeto (ver CLAUDE.md). Se a mudança é visível na UI, valide também no navegador (dev server na porta 5173).
