<template>
  <div class="avaliacao-lista">
    <div class="avaliacao-lista__header">
      <div>
        <h1 class="avaliacao-lista__title">{{ $t('dashboardAvaliacoes.titulo') }}</h1>
        <p class="avaliacao-lista__subtitle">{{ $t('dashboardAvaliacoes.subtitulo') }}</p>
      </div>
      <NuvexaButton variant="primary" icon="mdi-plus" @click="onCreate">{{ $t('dashboardAvaliacoes.nova') }}</NuvexaButton>
    </div>

    <NuvexaSummaryCards class="avaliacao-lista__section" :cards="summaryCards" />

    <NuvexaFilterCard
      class="avaliacao-lista__section"
      :title="$t('dashboardAvaliacoes.filtros.titulo') as string"
      :search="draftBusca"
      @update:search="draftBusca = $event"
      @clear-search="limparBusca"
      @submit="pesquisar"
      @clear="limpar"
      :search-label="$t('dashboardAvaliacoes.filtros.buscaLabel') as string"
      :search-placeholder="$t('dashboardAvaliacoes.buscarPlaceholder') as string"
      :clear-label="$t('acao.limpar') as string"
      :search-button-label="$t('acao.buscar') as string"
    >
      <NuvexaSelect
        v-model="draftTipo"
        :items="opcoesTipo"
        :label="$t('dashboardAvaliacoes.filtros.tipo') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaSelect
        v-model="draftStatus"
        :items="opcoesStatus"
        :label="$t('dashboardAvaliacoes.filtros.status') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaPeriodoSelect v-model="draftPeriodo" :label="$t('dashboardAvaliacoes.filtros.periodo') as string" />
      <NuvexaSelect
        v-model="draftTendencia"
        :items="opcoesTendencia"
        :label="$t('dashboardAvaliacoes.filtros.tendencia') as string"
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaSelect
        v-model="draftAvaliador"
        :items="opcoesAvaliador"
        :label="$t('dashboardAvaliacoes.filtros.avaliador') as string"
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
      class="avaliacao-lista__section"
      :chips="chipsExibidos"
      :clear-all-label="$t('dashboardAvaliacoes.lista.limparTudo') as string"
      :remove-label="$t('dashboardAvaliacoes.lista.removerFiltro') as string"
      :result-label="resultLabel"
      :sort-options="opcoesOrdenacao"
      :sort-by="sort"
      @remove="removerChip($event)"
      @clear-all="limpar"
      @update:sort-by="sort = $event"
    />

    <v-card variant="flat" color="surface-variant" class="avaliacao-lista__table-card">
      <NuvexaDataTable
        :headers="headers"
        :items="filtrados"
        item-key="id"
        :loading="carregando"
        v-model:page="page"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('dashboardAvaliacoes.lista.itensPorPagina') as string"
        :of-label="$t('dashboardAvaliacoes.lista.de') as string"
        :total-items="filtrados.length"
      >
        <template #item.pacienteNome="{ item }">
          <div class="avaliacao-lista__nome-cell">
            <v-avatar color="primary" variant="tonal" size="31">
              <span class="avaliacao-lista__iniciais">{{ iniciais(item.pacienteNome) }}</span>
            </v-avatar>
            <span class="avaliacao-lista__nome-texto">{{ item.pacienteNome }}</span>
          </div>
        </template>

        <template #item.data="{ item }">{{ formatarData(item.data) }}</template>

        <template #item.tipo="{ item }">
          <NuvexaStatusChip :label="rotulos[item.tipo] ?? item.tipo" color="on-surface-variant" />
        </template>

        <template #item.peso="{ item }">
          <div v-if="item.peso != null" class="avaliacao-lista__peso-cell">
            <span class="avaliacao-lista__peso-valor">{{ formatarPeso(item.peso) }} kg</span>
            <span
              v-if="item.variacaoPeso != null"
              class="avaliacao-lista__peso-variacao"
              :class="item.variacaoPeso > 0 ? 'avaliacao-lista__peso-variacao--alta' : item.variacaoPeso < 0 ? 'avaliacao-lista__peso-variacao--queda' : ''"
            >
              {{ formatarVariacao(item.variacaoPeso) }} kg
            </span>
          </div>
          <span v-else>—</span>
        </template>

        <template #item.percentualGordura="{ item }">{{ item.percentualGordura != null ? `${formatarPeso(item.percentualGordura)}%` : '—' }}</template>

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
            v-if="avaliacoes.length === 0"
            icon="mdi-clipboard-pulse-outline"
            :title="$t('dashboardAvaliacoes.vazio.titulo') as string"
            :description="$t('dashboardAvaliacoes.vazio.descricao') as string"
            :action-label="$t('dashboardAvaliacoes.nova') as string"
            @action="onCreate"
          />
          <NuvexaEmptyState
            v-else
            icon="mdi-magnify"
            :title="$t('dashboardAvaliacoes.vazio.tituloFiltro') as string"
            :description="$t('dashboardAvaliacoes.vazio.descricaoFiltro') as string"
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
import avaliacaoService from '../../services/avaliacao-service'
import consultaService from '../../../service/consulta-service'
import type { Avaliacao, AvaliacaoRelatorioFiltro, StatusAvaliacao } from '../../types/avaliacao'
import type { Profissional } from '../../../types/consulta'
import { extrairMensagemErro } from '../../../util/api-util'
import { carregarRotulosAvaliacao } from '../../utils/avaliacao-rotulos'
import { dentroDoPeriodo, periodoLabelKey } from '../../../util/periodo'
import { useAppStore } from '../../../store/app.store'
import { useFiltros, type ActiveChip, type FilterSchema, type FiltrosUrl } from '../../../composables/useFilters'
import NuvexaSummaryCards from '../../../components/common/NuvexaSummaryCards.vue'
import type { SummaryCardItem } from '../../../components/common/NuvexaSummaryCards.vue'
import NuvexaFilterCard from '../../../components/common/NuvexaFilterCard.vue'
import NuvexaFilterChips from '../../../components/common/NuvexaFilterChips.vue'
import NuvexaDataTable, { type NuvexaTableHeader } from '../../../components/common/NuvexaDataTable.vue'
import NuvexaEmptyState from '../../../components/common/NuvexaEmptyState.vue'
import NuvexaSelect from '../../../components/common/NuvexaSelect.vue'
import NuvexaPeriodoSelect from '../../../components/common/NuvexaPeriodoSelect.vue'
import NuvexaStatusChip from '../../../components/common/NuvexaStatusChip.vue'
import NuvexaButton from '../../../components/common/NuvexaButton.vue'

const ITENS_POR_PAGINA_OPCOES = [10, 25, 50]

const TENDENCIA_VALUES = ['queda', 'estavel', 'alta'] as const
type TendenciaValor = (typeof TENDENCIA_VALUES)[number]

const STATUS_COLORS: Record<StatusAvaliacao, string> = {
  CONCLUIDA: 'success',
  AGENDADA: 'on-surface-variant',
}

function normalizar(texto: string): string {
  return (texto || '').toLowerCase()
}

function tendenciaDe(variacaoPeso: number | null): TendenciaValor {
  if (variacaoPeso == null || Math.abs(variacaoPeso) <= 0.1) return 'estavel'
  return variacaoPeso > 0 ? 'alta' : 'queda'
}

@Component({
  name: 'AvaliacaoLista',
  components: {
    NuvexaSummaryCards,
    NuvexaFilterCard,
    NuvexaFilterChips,
    NuvexaDataTable,
    NuvexaEmptyState,
    NuvexaSelect,
    NuvexaPeriodoSelect,
    NuvexaStatusChip,
    NuvexaButton,
  },
})
export default class AvaliacaoLista extends Vue {
  avaliacoes: Avaliacao[] = []
  profissionais: Profissional[] = []
  rotulos: Record<string, string> = {}
  enumsTipo: { value: string; label: string }[] = []
  enumsStatus: { value: string; label: string }[] = []
  carregando = false
  exportando = false

  draftBusca = ''
  draftTipo: string[] = []
  draftStatus: string[] = []
  draftPeriodo = 'hoje'
  draftTendencia: string[] = []
  draftAvaliador: string[] = []

  filtros!: FiltrosUrl<FilterSchema>

  created() {
    this.filtros = useFiltros(this, {
      q: { label: this.$t('dashboardAvaliacoes.lista.buscaChip') as string, type: 'text', default: '' },
      av_tipo: { label: this.$t('dashboardAvaliacoes.lista.tipoChip') as string, type: 'multi-enum' },
      av_status: { label: this.$t('dashboardAvaliacoes.lista.statusChip') as string, type: 'multi-enum' },
      av_periodo: { label: this.$t('dashboardAvaliacoes.lista.periodoChip') as string, type: 'enum', default: 'hoje' },
      av_tendencia: { label: this.$t('dashboardAvaliacoes.lista.tendenciaChip') as string, type: 'multi-enum' },
      av_avaliador: { label: this.$t('dashboardAvaliacoes.lista.avaliadorChip') as string, type: 'multi-enum' },
    })
    this.sincronizarDraftComAplicado()
  }

  async mounted() {
    this.carregando = true
    try {
      const [avaliacoes, profissionais, rotulos, enums] = await Promise.all([
        avaliacaoService.listar(),
        consultaService.listarProfissionais(),
        carregarRotulosAvaliacao(),
        avaliacaoService.enums(),
      ])
      this.avaliacoes = avaliacoes
      this.profissionais = profissionais
      this.rotulos = rotulos
      this.enumsTipo = enums.tipos.map((opcao) => ({ value: opcao.valor, label: opcao.rotulo }))
      this.enumsStatus = enums.status.map((opcao) => ({ value: opcao.valor, label: opcao.rotulo }))
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarAvaliacoes') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  get appStore() {
    return useAppStore()
  }

  get headers(): NuvexaTableHeader[] {
    return [
      { title: this.$t('dashboardAvaliacoes.tabela.paciente') as string, key: 'pacienteNome' },
      { title: this.$t('dashboardAvaliacoes.tabela.data') as string, key: 'data', sortable: false },
      { title: this.$t('dashboardAvaliacoes.tabela.tipo') as string, key: 'tipo', sortable: false },
      { title: this.$t('dashboardAvaliacoes.tabela.peso') as string, key: 'peso', sortable: false },
      { title: this.$t('dashboardAvaliacoes.tabela.gordura') as string, key: 'percentualGordura', sortable: false },
      { title: this.$t('dashboardAvaliacoes.tabela.status') as string, key: 'status', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get opcoesTipo() {
    return this.enumsTipo
  }

  get opcoesStatus() {
    return this.enumsStatus
  }

  get opcoesTendencia() {
    return TENDENCIA_VALUES.map((value) => ({ value, label: this.$t(`dashboardAvaliacoes.filtros.tendenciaOpcoes.${value}`) as string }))
  }

  get opcoesAvaliador() {
    return this.profissionais.map((profissional) => ({ value: String(profissional.id), label: profissional.nome }))
  }

  get opcoesOrdenacao() {
    return [
      { value: 'recent', label: this.$t('dashboardAvaliacoes.lista.ordenar.recentes') as string },
      { value: 'name', label: this.$t('dashboardAvaliacoes.lista.ordenar.pacienteAZ') as string },
    ]
  }

  get chipsExibidos(): ActiveChip[] {
    return this.filtros.activeChips.map((chip) => {
      if (chip.key === 'av_tipo' || chip.key === 'av_status') return { ...chip, value: this.rotulos[chip.rawValue] ?? chip.rawValue }
      if (chip.key === 'av_periodo') return { ...chip, value: this.$t(periodoLabelKey(chip.rawValue)) as string }
      if (chip.key === 'av_tendencia') return { ...chip, value: this.$t(`dashboardAvaliacoes.filtros.tendenciaOpcoes.${chip.rawValue}`) as string }
      if (chip.key === 'av_avaliador') return { ...chip, value: this.avaliadorLabel(chip.rawValue) }
      return chip
    })
  }

  get filtrados(): Avaliacao[] {
    const busca = normalizar(this.filtros.value('q'))
    const tipoSelecionado = this.filtros.values('av_tipo')
    const statusSelecionado = this.filtros.values('av_status')
    const periodo = this.filtros.value('av_periodo')
    const tendenciaSelecionada = this.filtros.values('av_tendencia')
    const avaliadorSelecionado = this.filtros.values('av_avaliador')

    const resultado = this.avaliacoes.filter((item) => {
      if (busca && !normalizar(item.pacienteNome).includes(busca)) return false
      if (tipoSelecionado.length && !tipoSelecionado.includes(item.tipo)) return false
      if (statusSelecionado.length && !statusSelecionado.includes(item.status)) return false
      if (!dentroDoPeriodo(item.data, periodo)) return false
      if (tendenciaSelecionada.length && !tendenciaSelecionada.includes(tendenciaDe(item.variacaoPeso))) return false
      if (avaliadorSelecionado.length && !avaliadorSelecionado.includes(String(item.avaliadorId))) return false
      return true
    })

    const sort = this.sort
    return [...resultado].sort((a, b) => (sort === 'name' ? a.pacienteNome.localeCompare(b.pacienteNome, 'pt-BR') : 0))
  }

  get resultLabel(): string {
    return `${this.$t('dashboardAvaliacoes.lista.mostrando')} ${this.filtrados.length} ${this.$t('dashboardAvaliacoes.lista.de')} ${this.avaliacoes.length}`
  }

  get summary() {
    const concluidas = this.avaliacoes.filter((item) => item.status === 'CONCLUIDA')
    const variacoes = concluidas.map((item) => item.variacaoPeso).filter((v): v is number => v != null)
    return {
      concluidas: concluidas.length,
      agendadas: this.avaliacoes.filter((item) => item.status === 'AGENDADA').length,
      variacaoMedia: variacoes.length ? variacoes.reduce((soma, v) => soma + v, 0) / variacoes.length : 0,
      total: this.avaliacoes.length,
    }
  }

  get summaryCards(): SummaryCardItem[] {
    return [
      { label: this.$t('dashboardAvaliacoes.resumo.concluidas') as string, value: this.summary.concluidas, icon: 'mdi-clipboard-check-outline', color: 'success' },
      { label: this.$t('dashboardAvaliacoes.resumo.agendadas') as string, value: this.summary.agendadas, icon: 'mdi-calendar-clock-outline', color: 'info' },
      { label: this.$t('dashboardAvaliacoes.resumo.variacaoMedia') as string, value: `${this.formatarVariacao(this.summary.variacaoMedia)} kg`, icon: 'mdi-trending-down', color: 'success' },
      { label: this.$t('dashboardAvaliacoes.resumo.total') as string, value: this.summary.total, icon: 'mdi-clipboard-text-multiple-outline', color: 'on-surface-variant' },
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

  avaliadorLabel(id: string): string {
    return this.profissionais.find((profissional) => String(profissional.id) === id)?.nome ?? id
  }

  statusColor(status: StatusAvaliacao): string {
    return STATUS_COLORS[status]
  }

  sincronizarDraftComAplicado() {
    this.draftBusca = this.filtros.value('q')
    this.draftTipo = this.filtros.values('av_tipo')
    this.draftStatus = this.filtros.values('av_status')
    this.draftPeriodo = this.filtros.value('av_periodo')
    this.draftTendencia = this.filtros.values('av_tendencia')
    this.draftAvaliador = this.filtros.values('av_avaliador')
  }

  async pesquisar() {
    await this.filtros.applyMany({
      q: this.draftBusca,
      av_tipo: this.draftTipo,
      av_status: this.draftStatus,
      av_periodo: this.draftPeriodo,
      av_tendencia: this.draftTendencia,
      av_avaliador: this.draftAvaliador,
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

  formatarData(iso: string): string {
    const [ano, mes, dia] = iso.split('-')
    return `${dia}/${mes}/${ano}`
  }

  formatarPeso(valor: number): string {
    return Number(valor).toFixed(1).replace('.', ',')
  }

  formatarVariacao(valor: number): string {
    const sinal = valor > 0 ? '+' : ''
    return `${sinal}${this.formatarPeso(valor)}`
  }

  onCreate() {
    this.$router.push('/avaliacoes/novo')
  }

  onView(avaliacao: Avaliacao) {
    this.$router.push(`/avaliacoes/${avaliacao.id}`)
  }

  onEdit(avaliacao: Avaliacao) {
    this.$router.push(`/avaliacoes/${avaliacao.id}/editar`)
  }

  async onDelete(avaliacao: Avaliacao) {
    if (!confirm(this.$t('paciente.confirmarExclusao') as string)) {
      return
    }
    try {
      await avaliacaoService.excluir(avaliacao.id)
      this.avaliacoes = this.avaliacoes.filter((item) => item.id !== avaliacao.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.excluido') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirAvaliacao') as string), erro: true })
    }
  }

  async exportar() {
    this.exportando = true
    try {
      const filtro: AvaliacaoRelatorioFiltro = {
        busca: this.filtros.value('q') || undefined,
        tipo: this.filtros.values('av_tipo').join(',') || undefined,
        status: this.filtros.values('av_status').join(',') || undefined,
        periodo: this.filtros.value('av_periodo') || undefined,
        avaliadorId: this.filtros.values('av_avaliador').join(',') || undefined,
        tendencia: this.filtros.values('av_tendencia').join(',') || undefined,
      }
      const blob = await avaliacaoService.relatorioExcel(filtro)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'avaliacoes.xlsx'
      link.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.exportarAvaliacoes') as string), erro: true })
    } finally {
      this.exportando = false
    }
  }
}
</script>

<style scoped lang="scss">
.avaliacao-lista {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.avaliacao-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.avaliacao-lista__title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.avaliacao-lista__subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.avaliacao-lista__section {
  width: 100%;
}

.avaliacao-lista__table-card {
  border-radius: 12px;
  padding: 4px 12px;
}

.avaliacao-lista__nome-cell {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.avaliacao-lista__iniciais {
  font-size: 0.68rem;
  font-weight: 700;
}

.avaliacao-lista__nome-texto {
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.avaliacao-lista__peso-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.avaliacao-lista__peso-valor {
  font-size: 0.85rem;
  font-weight: 700;
}

.avaliacao-lista__peso-variacao {
  font-size: 0.72rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.avaliacao-lista__peso-variacao--queda {
  color: rgb(var(--v-theme-success));
}

.avaliacao-lista__peso-variacao--alta {
  color: rgb(var(--v-theme-error));
}
</style>
