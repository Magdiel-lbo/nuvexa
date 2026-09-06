<template>
  <div class="consulta-lista">
    <div class="consulta-lista__header">
      <div>
        <h1 class="consulta-lista__title">{{ $t('dashboardConsultas.titulo') }}</h1>
        <p class="consulta-lista__subtitle">{{ $t('dashboardConsultas.subtitulo') }}</p>
      </div>
      <NuvexaButton variant="primary" icon="mdi-plus" @click="onCreate">{{ $t('dashboardConsultas.nova') }}</NuvexaButton>
    </div>

    <NuvexaSummaryCards class="consulta-lista__section" :cards="summaryCards" />

    <NuvexaFilterCard
      class="consulta-lista__section"
      :title="$t('dashboardConsultas.filtros.titulo') as string"
      :search="draftBusca"
      @update:search="draftBusca = $event"
      @clear-search="limparBusca"
      @submit="pesquisar"
      @clear="limpar"
      :search-label="$t('dashboardConsultas.filtros.buscaLabel') as string"
      :search-placeholder="$t('dashboardConsultas.filtros.buscarPlaceholder') as string"
      :clear-label="$t('acao.limpar') as string"
      :search-button-label="$t('acao.buscar') as string"
    >
      <NuvexaSelect
        v-model="draftStatus"
        :items="opcoesStatus"
        :label="$t('dashboardConsultas.filtros.status') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaSelect
        v-model="draftTipo"
        :items="opcoesTipo"
        :label="$t('dashboardConsultas.filtros.tipo') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaPeriodoSelect v-model="draftPeriodo" :label="$t('dashboardConsultas.filtros.periodo') as string" />
      <NuvexaSelect
        v-model="draftProfissional"
        :items="opcoesProfissional"
        :label="$t('dashboardConsultas.filtros.profissional') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />

      <template #extra>
        <NuvexaButton variant="secondary" icon="mdi-tray-arrow-down" :loading="exportando" @click="exportar">
          {{ $t('dashboardConsultas.exportar') as string }}
        </NuvexaButton>
      </template>
    </NuvexaFilterCard>

    <NuvexaFilterChips
      class="consulta-lista__section"
      :chips="chipsExibidos"
      :clear-all-label="$t('dashboardConsultas.lista.limparTudo') as string"
      :remove-label="$t('dashboardConsultas.lista.removerFiltro') as string"
      :result-label="resultLabel"
      :sort-options="opcoesOrdenacao"
      :sort-by="sort"
      @remove="removerChip($event)"
      @clear-all="limpar"
      @update:sort-by="sort = $event"
    />

    <v-card variant="flat" color="surface-variant" class="consulta-lista__table-card">
      <NuvexaDataTable
        :headers="headers"
        :items="filtrados"
        item-key="id"
        :loading="carregando"
        v-model:page="page"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('dashboardConsultas.lista.itensPorPagina') as string"
        :of-label="$t('dashboardConsultas.lista.de') as string"
        :total-items="filtrados.length"
      >
        <template #item.pacienteNome="{ item }">
          <div class="consulta-lista__nome-cell">
            <v-avatar color="primary" variant="tonal" size="31">
              <span class="consulta-lista__iniciais">{{ iniciais(item.pacienteNome) }}</span>
            </v-avatar>
            <span class="consulta-lista__nome-texto">{{ item.pacienteNome }}</span>
          </div>
        </template>

        <template #item.dataHora="{ item }">
          <div class="consulta-lista__data-cell">
            <span class="consulta-lista__data-texto">{{ formatarData(item.dataHora) }}</span>
            <span class="consulta-lista__data-sub">{{ formatarHora(item.dataHora) }}</span>
          </div>
        </template>

        <template #item.tipo="{ item }">
          <NuvexaStatusChip :label="$t(`consulta.tipo.${item.tipo}`) as string" color="on-surface-variant" />
        </template>

        <template #item.status="{ item }">
          <NuvexaStatusChip :label="$t(`consulta.status.${item.status}`) as string" :color="statusColor(item.status)" />
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
                <v-list-item prepend-icon="mdi-close-circle-outline" :title="$t('dashboardConsultas.acoes.cancelar')" @click="onCancel(item)" />
                <v-list-item prepend-icon="mdi-delete-outline" :title="$t('acao.excluir')" @click="onDelete(item)" />
              </v-list>
            </v-menu>
          </div>
        </template>

        <template #no-data>
          <NuvexaEmptyState
            v-if="consultas.length === 0"
            icon="mdi-calendar-blank-outline"
            :title="$t('dashboardConsultas.vazio.titulo') as string"
            :description="$t('dashboardConsultas.vazio.descricao') as string"
            :action-label="$t('dashboardConsultas.nova') as string"
            @action="onCreate"
          />
          <NuvexaEmptyState
            v-else
            icon="mdi-magnify"
            :title="$t('dashboardConsultas.vazio.tituloFiltro') as string"
            :description="$t('dashboardConsultas.vazio.descricaoFiltro') as string"
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
import consultaService from '../../service/consulta-service'
import type { Consulta, ConsultaRelatorioFiltro, ConsultaStatus, ConsultaTipo, Profissional } from '../../types/consulta'
import { extrairMensagemErro } from '../../util/api-util'
import { consultaStatusColor } from '../../util/consulta-status'
import { dentroDoPeriodo, periodoLabelKey } from '../../util/periodo'
import { useAppStore } from '../../store/app.store'
import { useFiltros, type ActiveChip, type FilterSchema, type FiltrosUrl } from '../../composables/useFilters'
import NuvexaSummaryCards from '../../components/common/NuvexaSummaryCards.vue'
import type { SummaryCardItem } from '../../components/common/NuvexaSummaryCards.vue'
import NuvexaFilterCard from '../../components/common/NuvexaFilterCard.vue'
import NuvexaFilterChips from '../../components/common/NuvexaFilterChips.vue'
import NuvexaDataTable, { type NuvexaTableHeader } from '../../components/common/NuvexaDataTable.vue'
import NuvexaEmptyState from '../../components/common/NuvexaEmptyState.vue'
import NuvexaSelect from '../../components/common/NuvexaSelect.vue'
import NuvexaPeriodoSelect from '../../components/common/NuvexaPeriodoSelect.vue'
import NuvexaStatusChip from '../../components/common/NuvexaStatusChip.vue'
import NuvexaButton from '../../components/common/NuvexaButton.vue'

const ITENS_POR_PAGINA_OPCOES = [10, 25, 50]

const STATUS_VALUES: ConsultaStatus[] = ['AGENDADA', 'CONFIRMADA', 'REALIZADA', 'CANCELADA', 'FALTOU']
const TIPO_VALUES: ConsultaTipo[] = ['PRIMEIRA_CONSULTA', 'RETORNO', 'AVALIACAO']

function normalizar(texto: string): string {
  return (texto || '').toLowerCase()
}

function isSameDay(a: Date, b: Date): boolean {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate()
}

@Component({
  name: 'ConsultaLista',
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
export default class ConsultaLista extends Vue {
  consultas: Consulta[] = []
  profissionais: Profissional[] = []
  carregando = false
  exportando = false

  draftBusca = ''
  draftStatus: string[] = []
  draftTipo: string[] = []
  draftPeriodo = 'hoje'
  draftProfissional: string[] = []

  filtros!: FiltrosUrl<FilterSchema>

  created() {
    this.filtros = useFiltros(this, {
      q: { label: this.$t('dashboardConsultas.lista.buscaChip') as string, type: 'text', default: '' },
      status: { label: this.$t('dashboardConsultas.lista.statusChip') as string, type: 'multi-enum' },
      tipo: { label: this.$t('dashboardConsultas.lista.tipoChip') as string, type: 'multi-enum' },
      periodo: { label: this.$t('dashboardConsultas.lista.periodoChip') as string, type: 'enum', default: 'hoje' },
      profissional: { label: this.$t('dashboardConsultas.lista.profissionalChip') as string, type: 'multi-enum' },
    })
    this.sincronizarDraftComAplicado()
  }

  async mounted() {
    this.carregando = true
    try {
      const [consultas, profissionais] = await Promise.all([consultaService.listar(), consultaService.listarProfissionais()])
      this.consultas = consultas
      this.profissionais = profissionais
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarConsultas') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  get appStore() {
    return useAppStore()
  }

  get headers(): NuvexaTableHeader[] {
    return [
      { title: this.$t('dashboardConsultas.tabela.paciente') as string, key: 'pacienteNome' },
      { title: this.$t('dashboardConsultas.tabela.dataHora') as string, key: 'dataHora', sortable: false },
      { title: this.$t('dashboardConsultas.tabela.tipo') as string, key: 'tipo', sortable: false },
      { title: this.$t('dashboardConsultas.tabela.status') as string, key: 'status', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get opcoesStatus() {
    return STATUS_VALUES.map((value) => ({ value, label: this.$t(`consulta.status.${value}`) as string }))
  }

  get opcoesTipo() {
    return TIPO_VALUES.map((value) => ({ value, label: this.$t(`consulta.tipo.${value}`) as string }))
  }

  get opcoesProfissional() {
    return this.profissionais.map((profissional) => ({ value: String(profissional.id), label: profissional.nome }))
  }

  get opcoesOrdenacao() {
    return [
      { value: 'recent', label: this.$t('dashboardConsultas.lista.ordenar.recentes') as string },
      { value: 'name', label: this.$t('dashboardConsultas.lista.ordenar.pacienteAZ') as string },
    ]
  }

  get chipsExibidos(): ActiveChip[] {
    return this.filtros.activeChips.map((chip) => {
      if (chip.key === 'status') return { ...chip, value: this.$t(`consulta.status.${chip.rawValue}`) as string }
      if (chip.key === 'tipo') return { ...chip, value: this.$t(`consulta.tipo.${chip.rawValue}`) as string }
      if (chip.key === 'periodo') return { ...chip, value: this.$t(periodoLabelKey(chip.rawValue)) as string }
      if (chip.key === 'profissional') return { ...chip, value: this.profissionalLabel(chip.rawValue) }
      return chip
    })
  }

  get filtrados(): Consulta[] {
    const busca = normalizar(this.filtros.value('q'))
    const statusSelecionados = this.filtros.values('status')
    const tipoSelecionados = this.filtros.values('tipo')
    const periodo = this.filtros.value('periodo')
    const profissionalSelecionados = this.filtros.values('profissional')

    const resultado = this.consultas.filter((consulta) => {
      if (busca && !normalizar(consulta.pacienteNome).includes(busca)) return false
      if (statusSelecionados.length && !statusSelecionados.includes(consulta.status)) return false
      if (tipoSelecionados.length && !tipoSelecionados.includes(consulta.tipo)) return false
      if (!dentroDoPeriodo(consulta.dataHora, periodo)) return false
      if (profissionalSelecionados.length && !profissionalSelecionados.includes(String(consulta.profissionalId))) return false
      return true
    })

    const sort = this.sort
    return [...resultado].sort((a, b) => (sort === 'name' ? a.pacienteNome.localeCompare(b.pacienteNome, 'pt-BR') : 0))
  }

  get resultLabel(): string {
    return `${this.$t('dashboardConsultas.lista.mostrando')} ${this.filtrados.length} ${this.$t('dashboardConsultas.lista.de')} ${this.consultas.length}`
  }

  get summary() {
    const agora = new Date()
    return {
      total: this.consultas.length,
      hoje: this.consultas.filter((c) => isSameDay(new Date(c.dataHora), agora)).length,
      confirmadas: this.consultas.filter((c) => c.status === 'CONFIRMADA').length,
      canceladas: this.consultas.filter((c) => c.status === 'CANCELADA' || c.status === 'FALTOU').length,
    }
  }

  get summaryCards(): SummaryCardItem[] {
    return [
      { label: this.$t('dashboardConsultas.resumo.total') as string, value: this.summary.total, icon: 'mdi-calendar-multiple', color: 'primary' },
      { label: this.$t('dashboardConsultas.resumo.hoje') as string, value: this.summary.hoje, icon: 'mdi-calendar-today', color: 'on-surface-variant' },
      { label: this.$t('dashboardConsultas.resumo.confirmadas') as string, value: this.summary.confirmadas, icon: 'mdi-calendar-check', color: 'info' },
      { label: this.$t('dashboardConsultas.resumo.canceladas') as string, value: this.summary.canceladas, icon: 'mdi-calendar-remove', color: 'error' },
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

  profissionalLabel(id: string): string {
    return this.profissionais.find((profissional) => String(profissional.id) === id)?.nome ?? id
  }

  statusColor(status: ConsultaStatus): string {
    return consultaStatusColor(status)
  }

  sincronizarDraftComAplicado() {
    this.draftBusca = this.filtros.value('q')
    this.draftStatus = this.filtros.values('status')
    this.draftTipo = this.filtros.values('tipo')
    this.draftPeriodo = this.filtros.value('periodo')
    this.draftProfissional = this.filtros.values('profissional')
  }

  async pesquisar() {
    await this.filtros.applyMany({
      q: this.draftBusca,
      status: this.draftStatus,
      tipo: this.draftTipo,
      periodo: this.draftPeriodo,
      profissional: this.draftProfissional,
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
    return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
  }

  formatarHora(iso: string): string {
    return new Date(iso).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  }

  onCreate() {
    this.$router.push('/consultas/novo')
  }

  onView(consulta: Consulta) {
    this.$router.push(`/consultas/${consulta.id}`)
  }

  onEdit(consulta: Consulta) {
    this.$router.push(`/consultas/${consulta.id}/editar`)
  }

  async onCancel(consulta: Consulta) {
    try {
      await consultaService.cancelar(consulta)
      await this.recarregar()
      this.appStore.setToast({ mensagem: `Consulta de ${consulta.pacienteNome} cancelada.`, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.cancelarConsulta') as string), erro: true })
    }
  }

  async onDelete(consulta: Consulta) {
    try {
      await consultaService.excluir(consulta.id)
      this.consultas = this.consultas.filter((item) => item.id !== consulta.id)
      this.appStore.setToast({ mensagem: `Consulta de ${consulta.pacienteNome} removida.`, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirConsulta') as string), erro: true })
    }
  }

  async recarregar() {
    this.consultas = await consultaService.listar()
  }

  async exportar() {
    this.exportando = true
    try {
      const filtro: ConsultaRelatorioFiltro = {
        busca: this.filtros.value('q') || undefined,
        status: this.filtros.values('status').join(',') || undefined,
        tipo: this.filtros.values('tipo').join(',') || undefined,
        periodo: this.filtros.value('periodo') || undefined,
        profissionalId: this.filtros.values('profissional').join(',') || undefined,
      }
      const blob = await consultaService.relatorioExcel(filtro)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'consultas.xlsx'
      link.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.exportarConsultas') as string), erro: true })
    } finally {
      this.exportando = false
    }
  }
}
</script>

<style scoped lang="scss">
.consulta-lista {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.consulta-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.consulta-lista__title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.consulta-lista__subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.consulta-lista__section {
  width: 100%;
}

.consulta-lista__table-card {
  border-radius: 12px;
  padding: 4px 12px;
}

.consulta-lista__nome-cell {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.consulta-lista__iniciais {
  font-size: 0.68rem;
  font-weight: 700;
}

.consulta-lista__nome-texto {
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.consulta-lista__data-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.consulta-lista__data-texto {
  font-size: 0.85rem;
  font-weight: 500;
}

.consulta-lista__data-sub {
  font-size: 0.72rem;
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
