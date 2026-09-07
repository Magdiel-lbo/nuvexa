<template>
  <div class="plano-alimentar-lista">
    <div class="plano-alimentar-lista__header">
      <div>
        <h1 class="plano-alimentar-lista__title">{{ $t('dashboardNutricao.titulo') }}</h1>
        <p class="plano-alimentar-lista__subtitle">{{ $t('dashboardNutricao.subtitulo') }}</p>
      </div>
      <NuvexaButton variant="primary" icon="mdi-plus" @click="onCreate">{{ $t('dashboardNutricao.novo') }}</NuvexaButton>
    </div>

    <NuvexaSummaryCards class="plano-alimentar-lista__section" :cards="summaryCards" />

    <NuvexaFilterCard
      class="plano-alimentar-lista__section"
      :title="$t('dashboardNutricao.filtros.titulo') as string"
      :search="draftBusca"
      @update:search="draftBusca = $event"
      @clear-search="limparBusca"
      @submit="pesquisar"
      @clear="limpar"
      :search-label="$t('dashboardNutricao.filtros.buscaLabel') as string"
      :search-placeholder="$t('dashboardNutricao.filtros.buscarPlaceholder') as string"
      :clear-label="$t('acao.limpar') as string"
      :search-button-label="$t('acao.buscar') as string"
    >
      <NuvexaSelect v-model="draftStatus" :items="opcoesStatus" :label="$t('dashboardNutricao.filtros.status') as string" clearable />
      <NuvexaSelect v-model="draftPlano" :items="opcoesPlano" :label="$t('dashboardNutricao.filtros.plano') as string" clearable />
      <NuvexaSelect
        v-model="draftKcal"
        :items="opcoesFaixaCalorica"
        :label="$t('dashboardNutricao.filtros.faixaCalorica') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaSelect
        v-model="draftRefeicoes"
        :items="opcoesRefeicoesPorDia"
        :label="$t('dashboardNutricao.filtros.refeicoesPorDia') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />

      <template #extra>
        <NuvexaButton variant="secondary" icon="mdi-tray-arrow-down" :loading="exportando" @click="exportar">
          {{ $t('acao.exportar') as string }}
        </NuvexaButton>
      </template>
    </NuvexaFilterCard>

    <NuvexaFilterChips
      class="plano-alimentar-lista__section"
      :chips="chipsExibidos"
      :clear-all-label="$t('dashboardNutricao.lista.limparTudo') as string"
      :remove-label="$t('dashboardNutricao.lista.removerFiltro') as string"
      :result-label="resultLabel"
      :sort-options="opcoesOrdenacao"
      :sort-by="sort"
      @remove="removerChip($event)"
      @clear-all="limpar"
      @update:sort-by="sort = $event"
    />

    <v-card variant="flat" color="surface-variant" class="plano-alimentar-lista__table-card">
      <NuvexaDataTable
        :headers="headers"
        :items="filtrados"
        item-key="id"
        :loading="carregando"
        v-model:page="page"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('dashboardNutricao.lista.itensPorPagina') as string"
        :of-label="$t('dashboardNutricao.lista.de') as string"
        :total-items="filtrados.length"
      >
        <template #item.pacienteNome="{ item }">
          <div class="plano-alimentar-lista__nome-cell">
            <v-avatar color="primary" variant="tonal" size="31">
              <span class="plano-alimentar-lista__iniciais">{{ iniciais(item.pacienteNome) }}</span>
            </v-avatar>
            <span class="plano-alimentar-lista__nome-texto">{{ item.pacienteNome }}</span>
          </div>
        </template>

        <template #item.nome="{ item }">
          <div class="plano-alimentar-lista__plano-cell">
            <span class="plano-alimentar-lista__plano-texto">{{ item.nome }}</span>
            <span class="plano-alimentar-lista__plano-sub">
              {{ $t('dashboardNutricao.tabela.inicioPrefixo') }} {{ formatarDataInicio(item.dataInicio) }}
            </span>
          </div>
        </template>

        <template #item.calorias="{ item }">
          <span class="plano-alimentar-lista__calorias">{{ item.calorias }} kcal</span>
        </template>

        <template #item.refeicoesPorDia="{ item }">{{ item.refeicoesPorDia }}</template>

        <template #item.status="{ item }">
          <NuvexaStatusChip :label="rotulos[item.status] ?? item.status" :color="statusColor(item.status)" />
        </template>

        <template #item.acoes="{ item }">
          <div class="d-flex justify-end ga-1">
            <v-tooltip :text="$t('dashboardPacientes.acoes.visualizar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-view-grid-outline" variant="text" density="comfortable" size="small" @click="onView(item)" />
              </template>
            </v-tooltip>
            <v-tooltip :text="$t('acao.editar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-pencil-outline" variant="text" density="comfortable" size="small" @click="onEdit(item)" />
              </template>
            </v-tooltip>
            <v-menu>
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-dots-vertical" variant="text" density="comfortable" size="small" :aria-label="$t('dashboardPacientes.acoes.maisOpcoes')" />
              </template>
              <v-list density="compact">
                <v-list-item prepend-icon="mdi-delete-outline" :title="$t('acao.excluir')" @click="onDelete(item)" />
              </v-list>
            </v-menu>
          </div>
        </template>

        <template #no-data>
          <NuvexaEmptyState
            v-if="planos.length === 0"
            icon="mdi-food-apple-outline"
            :title="$t('dashboardNutricao.vazio.titulo') as string"
            :description="$t('dashboardNutricao.vazio.descricao') as string"
            :action-label="$t('dashboardNutricao.novo') as string"
            @action="onCreate"
          />
          <NuvexaEmptyState
            v-else
            icon="mdi-magnify"
            :title="$t('dashboardNutricao.vazio.tituloFiltro') as string"
            :description="$t('dashboardNutricao.vazio.descricaoFiltro') as string"
            :secondary-action-label="$t('acao.limpar') as string"
            @secondary-action="limpar"
          />
        </template>
      </NuvexaDataTable>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import planoAlimentarService from '../../services/plano-alimentar-service.ts'
import type { PlanoAlimentar, PlanoAlimentarRelatorioFiltro, StatusPlanoAlimentar } from '../../types/plano-alimentar.ts'
import { extrairMensagemErro } from '../../../util/api-util.ts'
import { carregarRotulosPlanoAlimentar } from '../../utils/plano-alimentar-rotulos.ts'
import { useAppStore } from '../../../store/app.store.ts'
import { useFiltros, type ActiveChip, type FilterSchema, type FiltrosUrl } from '../../../composables/useFilters.ts'
import NuvexaSummaryCards from '../../../components/common/NuvexaSummaryCards.vue'
import type { SummaryCardItem } from '../../../components/common/NuvexaSummaryCards.vue'
import NuvexaFilterCard from '../../../components/common/NuvexaFilterCard.vue'
import NuvexaFilterChips from '../../../components/common/NuvexaFilterChips.vue'
import NuvexaDataTable, { type NuvexaTableHeader } from '../../../components/common/NuvexaDataTable.vue'
import NuvexaEmptyState from '../../../components/common/NuvexaEmptyState.vue'
import NuvexaSelect from '../../../components/common/NuvexaSelect.vue'
import NuvexaStatusChip from '../../../components/common/NuvexaStatusChip.vue'
import NuvexaButton from '../../../components/common/NuvexaButton.vue'

const ITENS_POR_PAGINA_OPCOES = [10, 25, 50]

const STATUS_COLORS: Record<StatusPlanoAlimentar, string> = {
  ATIVO: 'success',
  RASCUNHO: 'warning',
  ENCERRADO: 'on-surface-variant',
}

interface FaixaCaloricaDef {
  key: string
  test: (calorias: number) => boolean
}

const FAIXAS_CALORICAS: FaixaCaloricaDef[] = [
  { key: 'ate1600', test: (kcal) => kcal <= 1600 },
  { key: 'entre1600e2200', test: (kcal) => kcal > 1600 && kcal <= 2200 },
  { key: 'acima2200', test: (kcal) => kcal > 2200 },
]

const REFEICOES_VALUES = ['4', '5', '6']

function normalizar(texto: string): string {
  return (texto || '').toLowerCase()
}

function combinaFaixaCalorica(selecionadas: string[], calorias: number): boolean {
  return selecionadas.length === 0 || FAIXAS_CALORICAS.some((faixa) => selecionadas.includes(faixa.key) && faixa.test(calorias))
}

@Component({
  name: 'PlanoAlimentarLista',
  components: {
    NuvexaSummaryCards,
    NuvexaFilterCard,
    NuvexaFilterChips,
    NuvexaDataTable,
    NuvexaEmptyState,
    NuvexaSelect,
    NuvexaStatusChip,
    NuvexaButton,
  },
})
export default class PlanoAlimentarLista extends Vue {
  planos: PlanoAlimentar[] = []
  rotulos: Record<string, string> = {}
  enumsStatus: { value: string; label: string }[] = []
  carregando = false
  exportando = false

  draftBusca = ''
  draftStatus = ''
  draftPlano = ''
  draftKcal: string[] = []
  draftRefeicoes: string[] = []

  filtros!: FiltrosUrl<FilterSchema>

  created() {
    this.filtros = useFiltros(this, {
      q: { label: this.$t('dashboardNutricao.lista.buscaChip') as string, type: 'text', default: '' },
      nu_status: { label: this.$t('dashboardNutricao.lista.statusChip') as string, type: 'enum', default: '' },
      nu_plano: { label: this.$t('dashboardNutricao.lista.planoChip') as string, type: 'enum', default: '' },
      nu_kcal: { label: this.$t('dashboardNutricao.lista.kcalChip') as string, type: 'multi-enum' },
      nu_refeicoes: { label: this.$t('dashboardNutricao.lista.refeicoesChip') as string, type: 'multi-enum' },
    })
    this.sincronizarDraftComAplicado()
  }

  async mounted() {
    this.carregando = true
    try {
      const [planos, rotulos, enums] = await Promise.all([
        planoAlimentarService.listar(),
        carregarRotulosPlanoAlimentar(),
        planoAlimentarService.enums(),
      ])
      this.planos = planos
      this.rotulos = rotulos
      this.enumsStatus = enums.status.map((opcao) => ({ value: opcao.valor, label: opcao.rotulo }))
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPlanosAlimentares') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  get appStore() {
    return useAppStore()
  }

  get headers(): NuvexaTableHeader[] {
    return [
      { title: this.$t('dashboardNutricao.tabela.paciente') as string, key: 'pacienteNome' },
      { title: this.$t('dashboardNutricao.tabela.plano') as string, key: 'nome', sortable: false },
      { title: this.$t('dashboardNutricao.tabela.calorias') as string, key: 'calorias', sortable: false },
      { title: this.$t('dashboardNutricao.tabela.refeicoes') as string, key: 'refeicoesPorDia', sortable: false },
      { title: this.$t('dashboardNutricao.tabela.status') as string, key: 'status', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get opcoesStatus() {
    return this.enumsStatus
  }

  get opcoesPlano() {
    const nomes = [...new Set(this.planos.map((plano) => plano.nome))].sort((a, b) => a.localeCompare(b, 'pt-BR'))
    return nomes.map((nome) => ({ value: nome, label: nome }))
  }

  get opcoesFaixaCalorica() {
    return FAIXAS_CALORICAS.map((faixa) => ({
      value: faixa.key,
      label: this.$t(`dashboardNutricao.filtros.faixaCaloricaOpcoes.${faixa.key}`) as string,
    }))
  }

  get opcoesRefeicoesPorDia() {
    return REFEICOES_VALUES.map((value) => ({ value, label: value }))
  }

  get opcoesOrdenacao() {
    return [
      { value: 'recent', label: this.$t('dashboardNutricao.lista.ordenar.recentes') as string },
      { value: 'name', label: this.$t('dashboardNutricao.lista.ordenar.pacienteAZ') as string },
      { value: 'calorias', label: this.$t('dashboardNutricao.lista.ordenar.calorias') as string },
    ]
  }

  get chipsExibidos(): ActiveChip[] {
    return this.filtros.activeChips.map((chip) => {
      if (chip.key === 'nu_status') return { ...chip, value: this.rotulos[chip.rawValue] ?? chip.rawValue }
      if (chip.key === 'nu_kcal') return { ...chip, value: this.$t(`dashboardNutricao.filtros.faixaCaloricaOpcoes.${chip.rawValue}`) as string }
      return chip
    })
  }

  get filtrados(): PlanoAlimentar[] {
    const busca = normalizar(this.filtros.value('q'))
    const status = this.filtros.value('nu_status')
    const plano = this.filtros.value('nu_plano')
    const kcalSelecionadas = this.filtros.values('nu_kcal')
    const refeicoesSelecionadas = this.filtros.values('nu_refeicoes')

    const resultado = this.planos.filter((item) => {
      if (busca && !normalizar(item.nome).includes(busca) && !normalizar(item.pacienteNome).includes(busca)) return false
      if (status && item.status !== status) return false
      if (plano && item.nome !== plano) return false
      if (!combinaFaixaCalorica(kcalSelecionadas, item.calorias)) return false
      if (refeicoesSelecionadas.length && !refeicoesSelecionadas.includes(String(item.refeicoesPorDia))) return false
      return true
    })

    const sort = this.sort
    return [...resultado].sort((a, b) => {
      if (sort === 'name') return a.pacienteNome.localeCompare(b.pacienteNome, 'pt-BR')
      if (sort === 'calorias') return b.calorias - a.calorias
      return 0
    })
  }

  get resultLabel(): string {
    return `${this.$t('dashboardNutricao.lista.mostrando')} ${this.filtrados.length} ${this.$t('dashboardNutricao.lista.de')} ${this.planos.length}`
  }

  get summary() {
    return {
      ativos: this.planos.filter((p) => p.status === 'ATIVO').length,
      rascunhos: this.planos.filter((p) => p.status === 'RASCUNHO').length,
      mediaKcal: this.planos.length ? Math.round(this.planos.reduce((soma, p) => soma + p.calorias, 0) / this.planos.length) : 0,
      total: this.planos.length,
    }
  }

  get summaryCards(): SummaryCardItem[] {
    return [
      { label: this.$t('dashboardNutricao.resumo.ativos') as string, value: this.summary.ativos, icon: 'mdi-clipboard-check-outline', color: 'success' },
      { label: this.$t('dashboardNutricao.resumo.rascunhos') as string, value: this.summary.rascunhos, icon: 'mdi-clipboard-edit-outline', color: 'warning' },
      { label: this.$t('dashboardNutricao.resumo.mediaKcal') as string, value: this.summary.mediaKcal, icon: 'mdi-fire', color: 'info' },
      { label: this.$t('dashboardNutricao.resumo.total') as string, value: this.summary.total, icon: 'mdi-clipboard-text-multiple-outline', color: 'on-surface-variant' },
    ]
  }

  get sort(): string {
    const raw = this.$route.query.sort
    return typeof raw === 'string' ? raw : 'recent'
  }

  set sort(value: string) {
    const query = { ...this.$route.query, sort: value === 'recent' ? undefined : value, pagina: undefined }
    this.$router.replace({ query })
  }

  get page(): number {
    const raw = this.$route.query.pagina
    const numero = typeof raw === 'string' ? parseInt(raw, 10) : NaN
    return Number.isFinite(numero) && numero > 0 ? numero : 1
  }

  set page(value: number) {
    const query = { ...this.$route.query, pagina: value <= 1 ? undefined : String(value) }
    this.$router.replace({ query })
  }

  get porPagina(): number {
    const raw = this.$route.query.porPagina
    const numero = typeof raw === 'string' ? parseInt(raw, 10) : NaN
    return ITENS_POR_PAGINA_OPCOES.includes(numero) ? numero : 10
  }

  set porPagina(value: number) {
    const query = { ...this.$route.query, porPagina: value === 10 ? undefined : String(value), pagina: undefined }
    this.$router.replace({ query })
  }

  statusColor(status: StatusPlanoAlimentar): string {
    return STATUS_COLORS[status]
  }

  sincronizarDraftComAplicado() {
    this.draftBusca = this.filtros.value('q')
    this.draftStatus = this.filtros.value('nu_status')
    this.draftPlano = this.filtros.value('nu_plano')
    this.draftKcal = this.filtros.values('nu_kcal')
    this.draftRefeicoes = this.filtros.values('nu_refeicoes')
  }

  async pesquisar() {
    await this.filtros.applyMany({
      q: this.draftBusca,
      nu_status: this.draftStatus,
      nu_plano: this.draftPlano,
      nu_kcal: this.draftKcal,
      nu_refeicoes: this.draftRefeicoes,
    })
    this.sincronizarDraftComAplicado()
  }

  async limparBusca() {
    this.draftBusca = ''
    await this.filtros.setValue('q', '')
  }

  async limpar() {
    await this.filtros.clearAll()
    this.sincronizarDraftComAplicado()
  }

  async removerChip(chip: ActiveChip) {
    await this.filtros.removeChip(chip)
    this.sincronizarDraftComAplicado()
  }

  iniciais(nome: string): string {
    return nome
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((parte) => parte[0])
      .join('')
      .toUpperCase()
  }

  formatarDataInicio(isoData: string): string {
    const [ano, mes, dia] = isoData.split('-')
    return `${dia}/${mes}/${ano}`
  }

  onCreate() {
    this.$router.push('/nutricao/novo')
  }

  onView(plano: PlanoAlimentar) {
    this.$router.push(`/nutricao/${plano.id}`)
  }

  onEdit(plano: PlanoAlimentar) {
    this.$router.push(`/nutricao/${plano.id}/editar`)
  }

  async onDelete(plano: PlanoAlimentar) {
    if (!confirm(this.$t('paciente.confirmarExclusao') as string)) {
      return
    }
    try {
      await planoAlimentarService.excluir(plano.id)
      this.planos = this.planos.filter((item) => item.id !== plano.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.excluido') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirPlanoAlimentar') as string), erro: true })
    }
  }

  async exportar() {
    this.exportando = true
    try {
      const filtro: PlanoAlimentarRelatorioFiltro = {
        busca: this.filtros.value('q') || undefined,
        status: this.filtros.value('nu_status') || undefined,
        plano: this.filtros.value('nu_plano') || undefined,
        faixaCalorica: this.filtros.values('nu_kcal').join(',') || undefined,
        refeicoesPorDia: this.filtros.values('nu_refeicoes').join(',') || undefined,
      }
      const blob = await planoAlimentarService.relatorioExcel(filtro)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'planos-alimentares.xlsx'
      link.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.exportarPlanosAlimentares') as string), erro: true })
    } finally {
      this.exportando = false
    }
  }
}
</script>

<style scoped lang="scss">
.plano-alimentar-lista {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.plano-alimentar-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.plano-alimentar-lista__title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.plano-alimentar-lista__subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.plano-alimentar-lista__section {
  width: 100%;
}

.plano-alimentar-lista__table-card {
  border-radius: 12px;
  padding: 4px 12px;
}

.plano-alimentar-lista__nome-cell {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.plano-alimentar-lista__iniciais {
  font-size: 0.68rem;
  font-weight: 700;
}

.plano-alimentar-lista__nome-texto {
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plano-alimentar-lista__plano-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.plano-alimentar-lista__plano-texto {
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plano-alimentar-lista__plano-sub {
  font-size: 0.72rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.plano-alimentar-lista__calorias {
  font-size: 0.85rem;
  font-weight: 700;
}
</style>
