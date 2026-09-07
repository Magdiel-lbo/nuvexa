<template>
  <div class="paciente-lista">
    <div class="paciente-lista__header">
      <div>
        <h1 class="paciente-lista__title">{{ $t('paciente.titulo') }}</h1>
        <p class="paciente-lista__subtitle">{{ $t('paciente.subtitulo') }}</p>
      </div>
      <NuvexaButton variant="primary" icon="mdi-plus" @click="novo">
        {{ $t('paciente.novo') }}
      </NuvexaButton>
    </div>

    <NuvexaFilterCard
      class="paciente-lista__section"
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
      <NuvexaSelect v-model="draftObjetivo" :items="opcoesObjetivo" :label="$t('paciente.objetivo') as string" clearable />
      <NuvexaSelect
        v-model="draftImc"
        :items="opcoesFaixaImc"
        :label="$t('paciente.lista.faixaImc') as string"
        autocomplete
        multiple
        chips
        closable-chips
        clearable
      />

      <template #extra>
        <NuvexaButton variant="secondary" icon="mdi-tray-arrow-down" :loading="exportando" @click="exportar">
          {{ $t('acao.exportar') }}
        </NuvexaButton>
      </template>
    </NuvexaFilterCard>

    <NuvexaFilterChips
      class="paciente-lista__section"
      :chips="chipsExibidos"
      :clear-all-label="$t('paciente.lista.limparTudo') as string"
      :remove-label="$t('paciente.lista.removerFiltro') as string"
      :result-label="resultLabel"
      :sort-options="opcoesOrdenacao"
      :sort-by="sort"
      @remove="removerChip($event)"
      @clear-all="limpar"
      @update:sort-by="sort = $event"
    />

    <v-card variant="flat" color="surface-variant" class="paciente-lista__table-card">
      <NuvexaDataTable
        :headers="headers"
        :items="linhas"
        item-key="pacienteId"
        :loading="carregando"
        v-model:page="page"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('paciente.lista.itensPorPagina') as string"
        :of-label="$t('paciente.lista.de') as string"
        :total-items="filtrados.length"
      >
        <template #item.pacienteNome="{ item }">
          <div class="paciente-lista__nome-cell">
            <v-avatar color="primary" variant="tonal" size="31">
              <span class="paciente-lista__iniciais">{{ iniciais(item.pacienteNome) }}</span>
            </v-avatar>
            <div class="paciente-lista__nome-info">
              <span class="paciente-lista__nome-texto">{{ item.pacienteNome }}</span>
              <span v-if="item.ultimaConsultaLabel" class="paciente-lista__nome-sub">
                {{ $t('paciente.ultimaConsultaPrefixo') }} {{ item.ultimaConsultaLabel }}
              </span>
            </div>
          </div>
        </template>

        <template #item.sexo="{ item }">{{ rotulos[item.sexo] ?? item.sexo }}</template>
        <template #item.idade="{ item }">{{ item.idade }} {{ $t('paciente.detalhe.anos') }}</template>
        <template #item.objetivo="{ item }">{{ rotulos[item.objetivo] ?? item.objetivo }}</template>
        <template #item.imc="{ item }">{{ Number(item.imc).toFixed(2) }}</template>

        <template #item.acoes="{ item }">
          <div class="d-flex justify-end ga-1">
            <v-tooltip :text="$t('dashboardPacientes.acoes.visualizar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-view-grid-outline" variant="text" density="comfortable" size="small" @click="visualizar(item.pacienteId)" />
              </template>
            </v-tooltip>
            <v-tooltip :text="$t('acao.editar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-pencil-outline" variant="text" density="comfortable" size="small" @click="editar(item.pacienteId)" />
              </template>
            </v-tooltip>
            <v-menu v-if="podeExcluir">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-dots-vertical" variant="text" density="comfortable" size="small" :aria-label="$t('dashboardPacientes.acoes.maisOpcoes')" />
              </template>
              <v-list density="compact">
                <v-list-item prepend-icon="mdi-delete-outline" :title="$t('acao.excluir')" @click="excluir(item.pacienteId)" />
              </v-list>
            </v-menu>
          </div>
        </template>

        <template #no-data>
          <NuvexaEmptyState
            v-if="pacientes.length === 0"
            icon="mdi-account-plus-outline"
            :title="$t('paciente.lista.vazioTitulo') as string"
            :description="$t('paciente.lista.vazioDescricao') as string"
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
import perfilNutricionalService from '../../nutricao/services/perfil-nutricional-service'
import pacienteRelatorioService from '../../nutricao/services/paciente-relatorio-service'
import consultaService from '../../service/consulta-service'
import type { Sexo } from '../../types/paciente'
import type { Objetivo, PerfilNutricionalResponse } from '../../nutricao/types/perfil-nutricional'
import type { PacienteRelatorioFiltro } from '../../nutricao/types/paciente-relatorio'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { usePermissoes } from '../../core/permissions/permissoes'
import { carregarRotulosEnum } from '../../nutricao/utils/enum-rotulos'
import { useFiltros, type ActiveChip, type FilterSchema, type FiltrosUrl } from '../../composables/useFilters'
import NuvexaFilterCard from '../../components/common/NuvexaFilterCard.vue'
import NuvexaFilterChips from '../../components/common/NuvexaFilterChips.vue'
import NuvexaDataTable, { type NuvexaTableHeader } from '../../components/common/NuvexaDataTable.vue'
import NuvexaEmptyState from '../../components/common/NuvexaEmptyState.vue'
import NuvexaSelect from '../../components/common/NuvexaSelect.vue'
import NuvexaButton from '../../components/common/NuvexaButton.vue'

interface FaixaDef {
  key: string
  test: (valor: number) => boolean
}

const FAIXAS_IMC: FaixaDef[] = [
  { key: 'normal', test: (imc) => imc < 25 },
  { key: 'sobrepeso', test: (imc) => imc >= 25 && imc < 30 },
  { key: 'obesidade', test: (imc) => imc >= 30 },
]

const ITENS_POR_PAGINA_OPCOES = [10, 25, 50]

function normalizar(texto: string): string {
  return (texto || '').toLowerCase()
}

function combinaFaixa(selecionadas: string[], valor: number, faixas: FaixaDef[]): boolean {
  return selecionadas.length === 0 || faixas.some((faixa) => selecionadas.includes(faixa.key) && faixa.test(valor))
}

interface PacienteLinha extends PerfilNutricionalResponse {
  ultimaConsultaLabel: string | null
}

/**
 * Só lista pacientes que têm perfil nutricional cadastrado — mesmo comportamento de antes da
 * separação Paciente(core)/PerfilNutricional(nutricao): a fonte da listagem é o perfil, não o
 * paciente genérico, porque as colunas (Objetivo/IMC) são todas dado de nutrição.
 */
@Component({
  name: 'PacienteLista',
  components: { NuvexaFilterCard, NuvexaFilterChips, NuvexaDataTable, NuvexaEmptyState, NuvexaSelect, NuvexaButton },
})
export default class PacienteLista extends Vue {
  pacientes: PerfilNutricionalResponse[] = []
  rotulos: Record<string, string> = {}
  ultimasConsultas: Record<number, string> = {}
  carregando = false
  exportando = false

  draftBusca = ''
  draftSexo = ''
  draftObjetivo = ''
  draftImc: string[] = []

  private opcoesSexoRaw: { value: string; label: string }[] = []
  private opcoesObjetivoRaw: { value: string; label: string }[] = []

  filtros!: FiltrosUrl<FilterSchema>

  created() {
    this.filtros = useFiltros(this, {
      q: { label: this.$t('paciente.lista.buscaChip') as string, type: 'text', default: '' },
      sexo: { label: this.$t('paciente.lista.sexoChip') as string, type: 'enum', default: '' },
      objetivo: { label: this.$t('paciente.lista.objetivoChip') as string, type: 'enum', default: '' },
      imc: { label: this.$t('paciente.lista.imcChip') as string, type: 'multi-enum' },
    })
    this.sincronizarDraftComAplicado()
  }

  async mounted() {
    this.carregando = true
    try {
      const [rotulos, pacienteEnums, perfilEnums] = await Promise.all([
        carregarRotulosEnum(),
        pacienteService.enums(),
        perfilNutricionalService.enums(),
      ])
      this.rotulos = rotulos
      this.opcoesSexoRaw = pacienteEnums.sexos.map((opcao) => ({ value: opcao.valor, label: opcao.rotulo }))
      this.opcoesObjetivoRaw = perfilEnums.objetivos.map((opcao) => ({ value: opcao.valor, label: opcao.rotulo }))
      const [pacientes, consultas] = await Promise.all([perfilNutricionalService.listar(), consultaService.listar()])
      this.pacientes = pacientes

      const ultimas: Record<number, string> = {}
      for (const consulta of consultas) {
        const atual = ultimas[consulta.pacienteId]
        if (!atual || new Date(consulta.dataHora) > new Date(atual)) {
          ultimas[consulta.pacienteId] = consulta.dataHora
        }
      }
      this.ultimasConsultas = ultimas
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarPacientes') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  get appStore() {
    return useAppStore()
  }

  /** Só esconde as ações; quem barra de fato a exclusão é o backend. */
  get podeExcluir(): boolean {
    return usePermissoes().podeExcluirPaciente
  }

  get headers(): NuvexaTableHeader[] {
    return [
      { title: this.$t('paciente.nome') as string, key: 'pacienteNome' },
      { title: this.$t('paciente.sexo') as string, key: 'sexo', sortable: false },
      { title: this.$t('paciente.idade') as string, key: 'idade', sortable: false },
      { title: this.$t('paciente.objetivo') as string, key: 'objetivo', sortable: false },
      { title: this.$t('paciente.imc') as string, key: 'imc', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get opcoesSexo() {
    return this.opcoesSexoRaw
  }

  get opcoesObjetivo() {
    return this.opcoesObjetivoRaw
  }

  get opcoesFaixaImc() {
    return FAIXAS_IMC.map((faixa) => ({ value: faixa.key, label: this.$t(`paciente.lista.imcFaixas.${faixa.key}`) as string }))
  }

  get opcoesOrdenacao() {
    return [
      { value: 'recent', label: this.$t('paciente.lista.ordenar.recentes') as string },
      { value: 'name', label: this.$t('paciente.lista.ordenar.nomeAZ') as string },
      { value: 'imc', label: this.$t('paciente.lista.ordenar.imc') as string },
    ]
  }

  get chipsExibidos(): ActiveChip[] {
    return this.filtros.activeChips.map((chip) => {
      if (chip.key === 'sexo' || chip.key === 'objetivo') {
        return { ...chip, value: this.rotulos[chip.rawValue] ?? chip.rawValue }
      }
      if (chip.key === 'imc') {
        return { ...chip, value: this.$t(`paciente.lista.imcFaixas.${chip.rawValue}`) as string }
      }
      return chip
    })
  }

  get filtrados(): PerfilNutricionalResponse[] {
    const busca = normalizar(this.filtros.value('q'))
    const sexo = this.filtros.value('sexo') as Sexo | ''
    const objetivo = this.filtros.value('objetivo') as Objetivo | ''
    const imcSelecionado = this.filtros.values('imc')

    const resultado = this.pacientes.filter((paciente) => {
      if (busca && !normalizar(paciente.pacienteNome).includes(busca)) return false
      if (sexo && paciente.sexo !== sexo) return false
      if (objetivo && paciente.objetivo !== objetivo) return false
      if (!combinaFaixa(imcSelecionado, Number(paciente.imc), FAIXAS_IMC)) return false
      return true
    })

    const sort = this.sort
    return [...resultado].sort((a, b) => {
      if (sort === 'name') return a.pacienteNome.localeCompare(b.pacienteNome, 'pt-BR')
      if (sort === 'imc') return Number(a.imc) - Number(b.imc)
      return 0
    })
  }

  get linhas(): PacienteLinha[] {
    return this.filtrados.map((paciente) => ({
      ...paciente,
      ultimaConsultaLabel: this.ultimasConsultas[paciente.pacienteId] ? this.formatarData(this.ultimasConsultas[paciente.pacienteId]) : null,
    }))
  }

  get resultLabel(): string {
    return `${this.$t('paciente.lista.mostrando')} ${this.filtrados.length} ${this.$t('paciente.lista.de')} ${this.pacientes.length}`
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

  sincronizarDraftComAplicado() {
    this.draftBusca = this.filtros.value('q')
    this.draftSexo = this.filtros.value('sexo')
    this.draftObjetivo = this.filtros.value('objetivo')
    this.draftImc = this.filtros.values('imc')
  }

  async pesquisar() {
    await this.filtros.applyMany({
      q: this.draftBusca,
      sexo: this.draftSexo,
      objetivo: this.draftObjetivo,
      imc: this.draftImc,
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

  novo() {
    this.$router.push('/pacientes/novo')
  }

  visualizar(id: number) {
    this.$router.push(`/pacientes/${id}`)
  }

  editar(id: number) {
    this.$router.push(`/pacientes/${id}/editar`)
  }

  async excluir(id: number) {
    if (!confirm(this.$t('paciente.confirmarExclusao') as string)) {
      return
    }
    try {
      await perfilNutricionalService.excluir(id)
      this.pacientes = this.pacientes.filter((paciente) => paciente.pacienteId !== id)
      this.appStore.setToast({ mensagem: this.$t('sucesso.excluido') as string, erro: false })
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.excluirPaciente') as string), erro: true })
    }
  }

  async exportar() {
    this.exportando = true
    try {
      const filtro: PacienteRelatorioFiltro = {
        busca: this.filtros.value('q') || undefined,
        sexo: (this.filtros.value('sexo') || undefined) as Sexo | undefined,
        objetivo: (this.filtros.value('objetivo') || undefined) as Objetivo | undefined,
      }
      const blob = await pacienteRelatorioService.relatorioExcel(filtro)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'pacientes.xlsx'
      link.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.exportarPacientes') as string), erro: true })
    } finally {
      this.exportando = false
    }
  }
}
</script>

<style scoped lang="scss">
@use '../../components/common/nuvexa-field.scss';

.paciente-lista {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.paciente-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.paciente-lista__title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.paciente-lista__subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.paciente-lista__section {
  width: 100%;
}

.paciente-lista__table-card {
  border-radius: 12px;
  padding: 4px 12px;
}

.paciente-lista__nome-cell {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.paciente-lista__iniciais {
  font-size: 0.68rem;
  font-weight: 700;
}

.paciente-lista__nome-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.paciente-lista__nome-texto {
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.paciente-lista__nome-sub {
  font-size: 0.72rem;
  color: rgb(var(--v-theme-on-surface-variant));
}
</style>
