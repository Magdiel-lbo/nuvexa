<template>
  <div class="prontuario-lista">
    <div class="prontuario-lista__header">
      <div>
        <h1 class="prontuario-lista__title">{{ $t('dashboardProntuarios.titulo') }}</h1>
        <p class="prontuario-lista__subtitle">{{ $t('dashboardProntuarios.subtitulo') }}</p>
      </div>
      <NuvexaButton variant="primary" icon="mdi-plus" @click="onCreate">{{ $t('dashboardProntuarios.novo') }}</NuvexaButton>
    </div>

    <NuvexaSummaryCards class="prontuario-lista__section" :cards="summaryCards" />

    <NuvexaFilterCard
      class="prontuario-lista__section"
      :title="$t('dashboardProntuarios.filtros.titulo') as string"
      :search="draftBusca"
      @update:search="draftBusca = $event"
      @clear-search="limparBusca"
      @submit="pesquisar"
      @clear="limpar"
      :search-label="$t('dashboardProntuarios.filtros.buscaLabel') as string"
      :search-placeholder="$t('dashboardProntuarios.filtros.buscarPlaceholder') as string"
      :clear-label="$t('acao.limpar') as string"
      :search-button-label="$t('acao.buscar') as string"
    >
      <NuvexaSelect
        v-model="draftStatus"
        :items="opcoesStatus"
        :label="$t('dashboardProntuarios.filtros.status') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaSelect
        v-model="draftSecao"
        :items="opcoesSecao"
        :label="$t('dashboardProntuarios.filtros.secao') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaPeriodoSelect v-model="draftPeriodo" :label="$t('dashboardProntuarios.filtros.periodo') as string" />
      <NuvexaSelect
        v-model="draftAutor"
        :items="opcoesAutor"
        :label="$t('dashboardProntuarios.filtros.autor') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />
      <NuvexaSelect
        v-model="draftAnexo"
        :items="opcoesAnexo"
        :label="$t('dashboardProntuarios.filtros.anexo') as string"
        multiple
        chips
        closable-chips
        clearable
      />
    </NuvexaFilterCard>

    <NuvexaFilterChips
      class="prontuario-lista__section"
      :chips="chipsExibidos"
      :clear-all-label="$t('dashboardProntuarios.lista.limparTudo') as string"
      :remove-label="$t('dashboardProntuarios.lista.removerFiltro') as string"
      :result-label="resultLabel"
      :sort-options="opcoesOrdenacao"
      :sort-by="sort"
      @remove="removerChip($event)"
      @clear-all="limpar"
      @update:sort-by="sort = $event"
    />

    <v-card variant="flat" color="surface-variant" class="prontuario-lista__table-card">
      <NuvexaDataTable
        :headers="headers"
        :items="filtrados"
        item-key="id"
        :loading="carregando"
        v-model:page="page"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('dashboardProntuarios.lista.itensPorPagina') as string"
        :of-label="$t('dashboardProntuarios.lista.de') as string"
        :total-items="filtrados.length"
      >
        <template #item.pacienteNome="{ item }">
          <div class="prontuario-lista__nome-cell">
            <v-avatar color="primary" variant="tonal" size="31">
              <span class="prontuario-lista__iniciais">{{ iniciais(item.pacienteNome) }}</span>
            </v-avatar>
            <div class="prontuario-lista__nome-info">
              <span class="prontuario-lista__nome-texto">{{ item.pacienteNome }}</span>
              <span class="prontuario-lista__nome-sub">
                {{ $t('dashboardProntuarios.tabela.atualizadoPrefixo') }} {{ formatarData(item.atualizadoEm) }}
              </span>
            </div>
          </div>
        </template>

        <template #item.registro="{ item }">{{ item.registro }}</template>

        <template #item.secao="{ item }">
          <NuvexaStatusChip :label="rotulos[item.secao] ?? item.secao" color="on-surface-variant" />
        </template>

        <template #item.autorNome="{ item }">{{ item.autorNome }}</template>

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
            v-if="prontuarios.length === 0"
            icon="mdi-file-document-outline"
            :title="$t('dashboardProntuarios.vazio.titulo') as string"
            :description="$t('dashboardProntuarios.vazio.descricao') as string"
            :action-label="$t('dashboardProntuarios.novo') as string"
            @action="onCreate"
          />
          <NuvexaEmptyState
            v-else
            icon="mdi-magnify"
            :title="$t('dashboardProntuarios.vazio.tituloFiltro') as string"
            :description="$t('dashboardProntuarios.vazio.descricaoFiltro') as string"
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
import prontuarioService from '../../service/prontuario-service'
import consultaService from '../../service/consulta-service'
import type { Prontuario, StatusProntuario } from '../../types/prontuario'
import type { EnumOpcao } from '../../util/enum-rotulos'
import type { Profissional } from '../../types/consulta'
import { extrairMensagemErro } from '../../util/api-util'
import { carregarRotulosProntuario } from '../../util/prontuario-rotulos'
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

const ANEXO_VALUES = ['sim', 'nao'] as const
type AnexoValor = (typeof ANEXO_VALUES)[number]

const STATUS_COLORS: Record<StatusProntuario, string> = {
  RASCUNHO: 'warning',
  PENDENTE: 'warning',
  ASSINADO: 'success',
}

function normalizar(texto: string): string {
  return (texto || '').toLowerCase()
}

@Component({
  name: 'ProntuarioLista',
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
export default class ProntuarioLista extends Vue {
  prontuarios: Prontuario[] = []
  profissionais: Profissional[] = []
  enumsSecao: EnumOpcao[] = []
  enumsStatus: EnumOpcao[] = []
  rotulos: Record<string, string> = {}
  carregando = false

  draftBusca = ''
  draftStatus: string[] = []
  draftSecao: string[] = []
  draftPeriodo = 'hoje'
  draftAutor: string[] = []
  draftAnexo: string[] = []

  filtros!: FiltrosUrl<FilterSchema>

  created() {
    this.filtros = useFiltros(this, {
      q: { label: this.$t('dashboardProntuarios.lista.buscaChip') as string, type: 'text', default: '' },
      pr_status: { label: this.$t('dashboardProntuarios.lista.statusChip') as string, type: 'multi-enum' },
      pr_secao: { label: this.$t('dashboardProntuarios.lista.secaoChip') as string, type: 'multi-enum' },
      pr_periodo: { label: this.$t('dashboardProntuarios.lista.periodoChip') as string, type: 'enum', default: 'hoje' },
      pr_autor: { label: this.$t('dashboardProntuarios.lista.autorChip') as string, type: 'multi-enum' },
      pr_anexo: { label: this.$t('dashboardProntuarios.lista.anexoChip') as string, type: 'multi-enum' },
    })
    this.sincronizarDraftComAplicado()
  }

  async mounted() {
    this.carregando = true
    try {
      const [prontuarios, profissionais, enums, rotulos] = await Promise.all([
        prontuarioService.listar(),
        consultaService.listarProfissionais(),
        prontuarioService.enums(),
        carregarRotulosProntuario(),
      ])
      this.prontuarios = prontuarios
      this.profissionais = profissionais
      this.enumsSecao = enums.secoes
      this.enumsStatus = enums.status
      this.rotulos = rotulos
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarProntuarios') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  get appStore() {
    return useAppStore()
  }

  get headers(): NuvexaTableHeader[] {
    return [
      { title: this.$t('dashboardProntuarios.tabela.paciente') as string, key: 'pacienteNome' },
      { title: this.$t('dashboardProntuarios.tabela.registro') as string, key: 'registro', sortable: false },
      { title: this.$t('dashboardProntuarios.tabela.secao') as string, key: 'secao', sortable: false },
      { title: this.$t('dashboardProntuarios.tabela.autor') as string, key: 'autorNome', sortable: false },
      { title: this.$t('dashboardProntuarios.tabela.status') as string, key: 'status', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get opcoesStatus() {
    return this.enumsStatus.map((opcao) => ({ value: opcao.valor, label: opcao.rotulo }))
  }

  get opcoesSecao() {
    return this.enumsSecao.map((opcao) => ({ value: opcao.valor, label: opcao.rotulo }))
  }

  get opcoesAutor() {
    return this.profissionais.map((profissional) => ({ value: String(profissional.id), label: profissional.nome }))
  }

  get opcoesAnexo() {
    return ANEXO_VALUES.map((value) => ({ value, label: this.anexoLabel(value) }))
  }

  get opcoesOrdenacao() {
    return [
      { value: 'recent', label: this.$t('dashboardProntuarios.lista.ordenar.recentes') as string },
      { value: 'name', label: this.$t('dashboardProntuarios.lista.ordenar.pacienteAZ') as string },
    ]
  }

  get chipsExibidos(): ActiveChip[] {
    return this.filtros.activeChips.map((chip) => {
      if (chip.key === 'pr_status') return { ...chip, value: this.rotulos[chip.rawValue] ?? chip.rawValue }
      if (chip.key === 'pr_secao') return { ...chip, value: this.rotulos[chip.rawValue] ?? chip.rawValue }
      if (chip.key === 'pr_periodo') return { ...chip, value: this.$t(periodoLabelKey(chip.rawValue)) as string }
      if (chip.key === 'pr_autor') return { ...chip, value: this.autorLabel(chip.rawValue) }
      if (chip.key === 'pr_anexo') return { ...chip, value: this.anexoLabel(chip.rawValue as AnexoValor) }
      return chip
    })
  }

  get filtrados(): Prontuario[] {
    const busca = normalizar(this.filtros.value('q'))
    const statusSelecionados = this.filtros.values('pr_status')
    const secaoSelecionadas = this.filtros.values('pr_secao')
    const periodo = this.filtros.value('pr_periodo')
    const autorSelecionados = this.filtros.values('pr_autor')
    const anexoSelecionados = this.filtros.values('pr_anexo')

    const resultado = this.prontuarios.filter((prontuario) => {
      if (busca && !normalizar(prontuario.pacienteNome).includes(busca)) return false
      if (statusSelecionados.length && !statusSelecionados.includes(prontuario.status)) return false
      if (secaoSelecionadas.length && !secaoSelecionadas.includes(prontuario.secao)) return false
      if (!dentroDoPeriodo(prontuario.atualizadoEm, periodo)) return false
      if (autorSelecionados.length && !autorSelecionados.includes(String(prontuario.autorId))) return false
      if (anexoSelecionados.length) {
        const valor = prontuario.comAnexo ? 'sim' : 'nao'
        if (!anexoSelecionados.includes(valor)) return false
      }
      return true
    })

    const sort = this.sort
    return [...resultado].sort((a, b) => (sort === 'name' ? a.pacienteNome.localeCompare(b.pacienteNome, 'pt-BR') : 0))
  }

  get resultLabel(): string {
    return `${this.$t('dashboardProntuarios.lista.mostrando')} ${this.filtrados.length} ${this.$t('dashboardProntuarios.lista.de')} ${this.prontuarios.length}`
  }

  get summary() {
    return {
      total: this.prontuarios.length,
      assinados: this.prontuarios.filter((p) => p.status === 'ASSINADO').length,
      rascunhos: this.prontuarios.filter((p) => p.status === 'RASCUNHO').length,
      pendentes: this.prontuarios.filter((p) => p.status === 'PENDENTE').length,
    }
  }

  get summaryCards(): SummaryCardItem[] {
    return [
      { label: this.$t('dashboardProntuarios.resumo.total') as string, value: this.summary.total, icon: 'mdi-file-document-multiple-outline', color: 'primary' },
      { label: this.$t('dashboardProntuarios.resumo.assinados') as string, value: this.summary.assinados, icon: 'mdi-file-check-outline', color: 'success' },
      { label: this.$t('dashboardProntuarios.resumo.rascunhos') as string, value: this.summary.rascunhos, icon: 'mdi-file-edit-outline', color: 'warning' },
      { label: this.$t('dashboardProntuarios.resumo.pendentes') as string, value: this.summary.pendentes, icon: 'mdi-file-alert-outline', color: 'error' },
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

  anexoLabel(valor: string): string {
    const chave: Record<AnexoValor, string> = {
      sim: 'dashboardProntuarios.filtros.anexoOpcoes.sim',
      nao: 'dashboardProntuarios.filtros.anexoOpcoes.nao',
    }
    return valor in chave ? (this.$t(chave[valor as AnexoValor]) as string) : valor
  }

  autorLabel(id: string): string {
    return this.profissionais.find((profissional) => String(profissional.id) === id)?.nome ?? id
  }

  statusColor(status: StatusProntuario): string {
    return STATUS_COLORS[status]
  }

  sincronizarDraftComAplicado() {
    this.draftBusca = this.filtros.value('q')
    this.draftStatus = this.filtros.values('pr_status')
    this.draftSecao = this.filtros.values('pr_secao')
    this.draftPeriodo = this.filtros.value('pr_periodo')
    this.draftAutor = this.filtros.values('pr_autor')
    this.draftAnexo = this.filtros.values('pr_anexo')
  }

  async pesquisar() {
    await this.filtros.applyMany({
      q: this.draftBusca,
      pr_status: this.draftStatus,
      pr_secao: this.draftSecao,
      pr_periodo: this.draftPeriodo,
      pr_autor: this.draftAutor,
      pr_anexo: this.draftAnexo,
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

  onCreate() {
    this.$router.push('/prontuarios/novo')
  }

  onView(prontuario: Prontuario) {
    this.$router.push(`/prontuarios/${prontuario.id}`)
  }

  onEdit(prontuario: Prontuario) {
    this.$router.push(`/prontuarios/${prontuario.id}/editar`)
  }

  async onDelete(prontuario: Prontuario) {
    if (!confirm(this.$t('paciente.confirmarExclusao') as string)) {
      return
    }
    try {
      await prontuarioService.excluir(prontuario.id)
      this.prontuarios = this.prontuarios.filter((item) => item.id !== prontuario.id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.excluido') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirProntuario') as string), erro: true })
    }
  }
}
</script>

<style scoped lang="scss">
.prontuario-lista {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.prontuario-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.prontuario-lista__title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.prontuario-lista__subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.prontuario-lista__section {
  width: 100%;
}

.prontuario-lista__table-card {
  border-radius: 12px;
  padding: 4px 12px;
}

.prontuario-lista__nome-cell {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.prontuario-lista__iniciais {
  font-size: 0.68rem;
  font-weight: 700;
}

.prontuario-lista__nome-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.prontuario-lista__nome-texto {
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.prontuario-lista__nome-sub {
  font-size: 0.72rem;
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
