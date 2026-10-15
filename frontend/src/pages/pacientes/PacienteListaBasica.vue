<template>
  <div class="paciente-lista-basica">
    <div class="paciente-lista-basica__header">
      <div>
        <h1 class="paciente-lista-basica__title">{{ $t('paciente.titulo') }}</h1>
        <p class="paciente-lista-basica__subtitle">{{ $t('paciente.basico.subtitulo') }}</p>
      </div>
      <NuvexaButton variant="primary" icon="mdi-plus" @click="novo">{{ $t('paciente.novo') }}</NuvexaButton>
    </div>

    <NuvexaFilterCard
      class="paciente-lista-basica__section"
      :title="$t('paciente.lista.filtrosTitulo') as string"
      :search="draftBusca"
      @update:search="draftBusca = $event"
      @clear-search="limparBusca"
      @submit="pesquisar"
      @clear="limpar"
      :search-label="$t('paciente.lista.buscaLabel') as string"
      :search-placeholder="$t('paciente.buscarPlaceholder') as string"
      :clear-label="$t('acao.limpar') as string"
      :search-button-label="$t('acao.buscar') as string"
    >
      <NuvexaSelect v-model="draftSexo" :items="opcoesSexo" :label="$t('paciente.sexo') as string" clearable />
    </NuvexaFilterCard>

    <NuvexaFilterChips
      class="paciente-lista-basica__section"
      :chips="filtros.activeChips"
      :clear-all-label="$t('paciente.lista.limparTudo') as string"
      :remove-label="$t('paciente.lista.removerFiltro') as string"
      :result-label="resultLabel"
      :sort-options="opcoesOrdenacao"
      :sort-by="sort"
      @remove="removerChip($event)"
      @clear-all="limpar"
      @update:sort-by="sort = $event"
    />

    <v-card variant="flat" color="surface-variant" class="paciente-lista-basica__table-card">
      <NuvexaDataTable
        :headers="headers"
        :items="filtrados"
        item-key="id"
        :loading="carregando"
        v-model:page="page"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('paciente.lista.itensPorPagina') as string"
        :of-label="$t('paciente.lista.de') as string"
        :total-items="filtrados.length"
      >
        <template #item.nome="{ item }">
          <div class="paciente-lista-basica__nome-cell">
            <v-avatar color="primary" variant="tonal" size="31"><span class="paciente-lista-basica__iniciais">{{ iniciais(item.nome) }}</span></v-avatar>
            <span class="paciente-lista-basica__nome-texto">{{ item.nome }}</span>
          </div>
        </template>
        <template #item.sexo="{ item }">{{ rotulosSexo[item.sexo] ?? item.sexo }}</template>
        <template #item.idade="{ item }">{{ item.idade }} {{ $t('paciente.detalhe.anos') }}</template>
        <template #item.acoes="{ item }">
          <div class="d-flex justify-end ga-1">
            <v-tooltip :text="$t('dashboardPacientes.acoes.visualizar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-view-grid-outline" variant="text" density="comfortable" size="small" @click="visualizar(item.id)" />
              </template>
            </v-tooltip>
            <v-tooltip :text="$t('acao.editar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-pencil-outline" variant="text" density="comfortable" size="small" @click="editar(item.id)" />
              </template>
            </v-tooltip>
          </div>
        </template>
        <template #no-data>
          <NuvexaEmptyState
            v-if="pacientes.length === 0"
            icon="mdi-account-plus-outline"
            :title="$t('paciente.lista.vazioTitulo') as string"
            :description="$t('paciente.basico.vazioDescricao') as string"
            :action-label="$t('paciente.novo') as string"
            @action="novo"
          />
          <NuvexaEmptyState
            v-else
            icon="mdi-magnify"
            :title="$t('paciente.lista.vazioFiltroTitulo') as string"
            :description="$t('paciente.lista.vazioFiltroDescricao') as string"
            :secondary-action-label="$t('paciente.lista.limparFiltros') as string"
            @secondary-action="limpar"
          />
        </template>
      </NuvexaDataTable>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import pacienteService from '../../service/paciente-service'
import type { PacienteResponse } from '../../types/paciente'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { useFiltros, type ActiveChip, type FilterSchema, type FiltrosUrl } from '../../composables/useFilters'
import NuvexaFilterCard from '../../components/common/NuvexaFilterCard.vue'
import NuvexaFilterChips from '../../components/common/NuvexaFilterChips.vue'
import NuvexaDataTable, { type NuvexaTableHeader } from '../../components/common/NuvexaDataTable.vue'
import NuvexaEmptyState from '../../components/common/NuvexaEmptyState.vue'
import NuvexaSelect from '../../components/common/NuvexaSelect.vue'
import NuvexaButton from '../../components/common/NuvexaButton.vue'

const ITENS_POR_PAGINA_OPCOES = [10, 25, 50]

/**
 * Lista de pacientes do núcleo (nome, idade, sexo), sem dado de nutrição. Usada por organizações
 * sem a vertical de nutrição e por quem não vê o clínico (recepção) — mesmo template de listagem.
 */
@Component({ name: 'PacienteListaBasica', components: { NuvexaFilterCard, NuvexaFilterChips, NuvexaDataTable, NuvexaEmptyState, NuvexaSelect, NuvexaButton } })
export default class PacienteListaBasica extends Vue {
  pacientes: PacienteResponse[] = []
  rotulosSexo: Record<string, string> = {}
  carregando = false

  draftBusca = ''
  draftSexo: string | null = null

  filtros!: FiltrosUrl<FilterSchema>

  created() {
    this.filtros = useFiltros(this, {
      q: { label: this.$t('paciente.lista.buscaChip') as string, type: 'text', default: '' },
      sexo: { label: this.$t('paciente.lista.sexoChip') as string, type: 'enum' },
    })
    this.sincronizarDraftComAplicado()
  }

  async mounted() {
    this.carregando = true
    try {
      const [pacientes, enums] = await Promise.all([pacienteService.listar(), pacienteService.enums()])
      this.pacientes = pacientes
      this.rotulosSexo = Object.fromEntries(enums.sexos.map((opcao) => [opcao.valor, opcao.rotulo]))
    } catch (e) {
      useAppStore().setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPacientes') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  get headers(): NuvexaTableHeader[] {
    return [
      { title: this.$t('paciente.nome') as string, key: 'nome' },
      { title: this.$t('paciente.sexo') as string, key: 'sexo', sortable: false },
      { title: this.$t('paciente.idade') as string, key: 'idade', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get opcoesSexo() {
    return Object.entries(this.rotulosSexo).map(([value, label]) => ({ value, label }))
  }

  get opcoesOrdenacao() {
    return [
      { value: 'name', label: this.$t('paciente.lista.ordenar.nomeAZ') as string },
      { value: 'idade', label: this.$t('paciente.basico.ordenarIdade') as string },
    ]
  }

  get filtrados(): PacienteResponse[] {
    const busca = this.filtros.value('q').toLowerCase()
    const sexo = this.filtros.value('sexo')
    const resultado = this.pacientes.filter((paciente) => (!busca || paciente.nome.toLowerCase().includes(busca)) && (!sexo || paciente.sexo === sexo))
    return [...resultado].sort((a, b) => (this.sort === 'idade' ? a.idade - b.idade : a.nome.localeCompare(b.nome, 'pt-BR')))
  }

  get resultLabel(): string {
    return `${this.$t('paciente.lista.mostrando')} ${this.filtrados.length} ${this.$t('paciente.lista.de')} ${this.pacientes.length}`
  }

  get sort(): string {
    const raw = this.$route.query.sort
    return typeof raw === 'string' ? raw : 'name'
  }

  set sort(value: string) {
    this.$router.replace({ query: { ...this.$route.query, sort: value === 'name' ? undefined : value, pagina: undefined } })
  }

  get page(): number {
    const numero = typeof this.$route.query.pagina === 'string' ? parseInt(this.$route.query.pagina, 10) : NaN
    return Number.isFinite(numero) && numero > 0 ? numero : 1
  }

  set page(value: number) {
    this.$router.replace({ query: { ...this.$route.query, pagina: value <= 1 ? undefined : String(value) } })
  }

  get porPagina(): number {
    const numero = typeof this.$route.query.porPagina === 'string' ? parseInt(this.$route.query.porPagina, 10) : NaN
    return ITENS_POR_PAGINA_OPCOES.includes(numero) ? numero : 10
  }

  set porPagina(value: number) {
    this.$router.replace({ query: { ...this.$route.query, porPagina: value === 10 ? undefined : String(value), pagina: undefined } })
  }

  sincronizarDraftComAplicado() {
    this.draftBusca = this.filtros.value('q')
    this.draftSexo = this.filtros.value('sexo') || null
  }

  async pesquisar() {
    await this.filtros.applyMany({ q: this.draftBusca, sexo: this.draftSexo ?? '' })
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
    return nome.trim().split(/\s+/).filter(Boolean).slice(0, 2).map((parte) => parte[0]).join('').toUpperCase()
  }

  novo() {
    this.$router.push('/pacientes/novo')
  }

  visualizar(id: number) {
    this.$router.push(`/pacientes/${id}`)
  }

  editar(id: number) {
    this.$router.push(`/pacientes/${id}/editar`)
  }
}
</script>

<style scoped lang="scss">
.paciente-lista-basica {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.paciente-lista-basica__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.paciente-lista-basica__title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.paciente-lista-basica__subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-lista-basica__section {
  width: 100%;
}

.paciente-lista-basica__table-card {
  border-radius: 12px;
  padding: 4px 12px;
}

.paciente-lista-basica__nome-cell {
  display: flex;
  align-items: center;
  gap: 11px;
}

.paciente-lista-basica__iniciais {
  font-size: 0.68rem;
  font-weight: 700;
}

.paciente-lista-basica__nome-texto {
  font-size: 0.85rem;
  font-weight: 600;
}
</style>
