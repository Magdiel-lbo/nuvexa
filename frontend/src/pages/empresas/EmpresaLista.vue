<template>
  <div class="empresa-lista">
    <div class="empresa-lista__header">
      <div>
        <h1 class="empresa-lista__title">{{ $t('dashboardEmpresas.titulo') }}</h1>
        <p class="empresa-lista__subtitle">{{ $t('dashboardEmpresas.subtitulo') }}</p>
      </div>
      <NuvexaButton variant="primary" icon="mdi-plus" @click="nova">{{ $t('dashboardEmpresas.nova') }}</NuvexaButton>
    </div>

    <NuvexaSummaryCards class="empresa-lista__section" :cards="summaryCards" />

    <NuvexaFilterCard
      class="empresa-lista__section"
      :title="$t('dashboardEmpresas.filtros.titulo') as string"
      :search="draftNomeFantasia"
      @update:search="draftNomeFantasia = $event"
      @clear-search="limparNomeFantasia"
      @submit="pesquisar"
      @clear="limpar"
      :search-label="$t('dashboardEmpresas.filtros.nomeFantasia') as string"
      :search-placeholder="$t('dashboardEmpresas.filtros.buscarPlaceholder') as string"
      :clear-label="$t('acao.limpar') as string"
      :search-button-label="$t('acao.buscar') as string"
    >
      <NuvexaField
        v-model="draftRazaoSocial"
        :label="$t('dashboardEmpresas.filtros.razaoSocial') as string"
        clearable
        @keyup.enter="pesquisar"
      />
      <NuvexaField v-model="draftCnpj" :label="$t('dashboardEmpresas.filtros.cnpj') as string" clearable @keyup.enter="pesquisar" />
    </NuvexaFilterCard>

    <NuvexaFilterChips
      class="empresa-lista__section"
      :chips="filtros.activeChips"
      :clear-all-label="$t('dashboardEmpresas.lista.limparTudo') as string"
      :remove-label="$t('dashboardEmpresas.lista.removerFiltro') as string"
      :result-label="resultLabel"
      :sort-options="opcoesOrdenacao"
      :sort-by="sort"
      @remove="removerChip($event)"
      @clear-all="limpar"
      @update:sort-by="sort = $event"
    />

    <v-card variant="flat" color="surface-variant" class="empresa-lista__table-card">
      <NuvexaDataTable
        :headers="headers"
        :items="filtrados"
        item-key="id"
        :loading="carregando"
        v-model:page="page"
        v-model:items-per-page="porPagina"
        :items-per-page-label="$t('dashboardEmpresas.lista.itensPorPagina') as string"
        :of-label="$t('dashboardEmpresas.lista.de') as string"
        :total-items="filtrados.length"
      >
        <template #item.nome="{ item }">
          <div class="empresa-lista__nome-cell">
            <v-avatar color="primary" variant="tonal" size="31">
              <span class="empresa-lista__iniciais">{{ iniciais(item.nome) }}</span>
            </v-avatar>
            <span class="empresa-lista__nome-texto">{{ item.nome }}</span>
          </div>
        </template>

        <template #item.razaoSocial="{ item }">
          {{ item.razaoSocial || '—' }}
        </template>

        <template #item.documento="{ item }">
          {{ item.documento || '—' }}
        </template>

        <template #item.tipo="{ item }">
          <NuvexaStatusChip :label="rotulo('tipos', item.tipo)" color="on-surface-variant" />
        </template>

        <template #item.especialidade="{ item }">
          {{ rotulo('especialidades', item.especialidade) }}
        </template>

        <template #item.status="{ item }">
          <NuvexaStatusChip :label="rotulo('status', item.status)" :color="item.status === 'ATIVA' ? 'success' : 'error'" />
        </template>

        <template #item.proprietarioAtivo="{ item }">
          <NuvexaStatusChip v-if="!item.proprietarioAtivo" :label="$t('dashboardEmpresas.tabela.semProprietario') as string" color="warning" />
          <span v-else>—</span>
        </template>

        <template #item.acoes="{ item }">
          <div class="d-flex justify-end">
            <v-tooltip :text="$t('acao.editar')" location="top">
              <template #activator="{ props }">
                <v-btn v-bind="props" icon="mdi-pencil-outline" variant="text" density="comfortable" size="small" @click="editar(item.id)" />
              </template>
            </v-tooltip>
          </div>
        </template>

        <template #no-data>
          <NuvexaEmptyState
            v-if="empresas.length === 0"
            icon="mdi-domain"
            :title="$t('dashboardEmpresas.vazio.titulo') as string"
            :description="$t('dashboardEmpresas.vazio.descricao') as string"
            :action-label="$t('dashboardEmpresas.nova') as string"
            @action="nova"
          />
          <NuvexaEmptyState
            v-else
            icon="mdi-magnify"
            :title="$t('dashboardEmpresas.vazio.tituloFiltro') as string"
            :description="$t('dashboardEmpresas.vazio.descricaoFiltro') as string"
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
import empresaService from '../../service/empresa-service'
import type { Empresa, EmpresaEnums } from '../../types/empresa'
import { extrairMensagemErro } from '../../util/api-util'
import { useAppStore } from '../../store/app.store'
import { useFiltros, type ActiveChip, type FilterSchema, type FiltrosUrl } from '../../composables/useFilters'
import NuvexaSummaryCards from '../../components/common/NuvexaSummaryCards.vue'
import type { SummaryCardItem } from '../../components/common/NuvexaSummaryCards.vue'
import NuvexaFilterCard from '../../components/common/NuvexaFilterCard.vue'
import NuvexaFilterChips from '../../components/common/NuvexaFilterChips.vue'
import NuvexaDataTable, { type NuvexaTableHeader } from '../../components/common/NuvexaDataTable.vue'
import NuvexaEmptyState from '../../components/common/NuvexaEmptyState.vue'
import NuvexaField from '../../components/common/NuvexaField.vue'
import NuvexaStatusChip from '../../components/common/NuvexaStatusChip.vue'
import NuvexaButton from '../../components/common/NuvexaButton.vue'

const ITENS_POR_PAGINA_OPCOES = [10, 25, 50]

function normalizar(texto: string | null): string {
  return (texto || '').toLowerCase()
}

function somenteDigitos(texto: string | null): string {
  return (texto || '').replace(/\D/g, '')
}

@Component({
  name: 'EmpresaLista',
  components: {
    NuvexaSummaryCards,
    NuvexaFilterCard,
    NuvexaFilterChips,
    NuvexaDataTable,
    NuvexaEmptyState,
    NuvexaField,
    NuvexaStatusChip,
    NuvexaButton,
  },
})
export default class EmpresaLista extends Vue {
  empresas: Empresa[] = []
  enums: EmpresaEnums = { tipos: [], especialidades: [], status: [] }
  carregando = false

  draftNomeFantasia = ''
  draftRazaoSocial = ''
  draftCnpj = ''

  filtros!: FiltrosUrl<FilterSchema>

  created() {
    this.filtros = useFiltros(this, {
      q: { label: this.$t('dashboardEmpresas.lista.nomeFantasiaChip') as string, type: 'text', default: '' },
      razaoSocial: { label: this.$t('dashboardEmpresas.lista.razaoSocialChip') as string, type: 'text', default: '' },
      cnpj: { label: this.$t('dashboardEmpresas.lista.cnpjChip') as string, type: 'text', default: '' },
    })
    this.sincronizarDraftComAplicado()
  }

  async mounted() {
    this.carregando = true
    try {
      const [empresas, enums] = await Promise.all([empresaService.listar(), empresaService.enums()])
      this.empresas = empresas
      this.enums = enums
    } catch (e) {
      this.appStore.setToast({ mensagem: extrairMensagemErro(e, this.$t('erro.carregarEmpresas') as string), erro: true })
    } finally {
      this.carregando = false
    }
  }

  get appStore() {
    return useAppStore()
  }

  get headers(): NuvexaTableHeader[] {
    return [
      { title: this.$t('dashboardEmpresas.tabela.nomeFantasia') as string, key: 'nome' },
      { title: this.$t('dashboardEmpresas.tabela.razaoSocial') as string, key: 'razaoSocial', sortable: false },
      { title: this.$t('dashboardEmpresas.tabela.cnpj') as string, key: 'documento', sortable: false },
      { title: this.$t('dashboardEmpresas.tabela.tipo') as string, key: 'tipo', sortable: false },
      { title: this.$t('dashboardEmpresas.tabela.especialidade') as string, key: 'especialidade', sortable: false },
      { title: this.$t('dashboardEmpresas.tabela.status') as string, key: 'status', sortable: false },
      { title: this.$t('dashboardEmpresas.tabela.proprietario') as string, key: 'proprietarioAtivo', sortable: false },
      { title: this.$t('acao.titulo') as string, key: 'acoes', sortable: false, align: 'end' },
    ]
  }

  get opcoesOrdenacao() {
    return [
      { value: 'recent', label: this.$t('dashboardEmpresas.lista.ordenar.recentes') as string },
      { value: 'name', label: this.$t('dashboardEmpresas.lista.ordenar.nomeAZ') as string },
    ]
  }

  get filtrados(): Empresa[] {
    const nome = normalizar(this.filtros.value('q'))
    const razaoSocial = normalizar(this.filtros.value('razaoSocial'))
    const cnpj = somenteDigitos(this.filtros.value('cnpj'))

    const resultado = this.empresas.filter((empresa) => {
      if (nome && !normalizar(empresa.nome).includes(nome)) return false
      if (razaoSocial && !normalizar(empresa.razaoSocial).includes(razaoSocial)) return false
      if (cnpj && !somenteDigitos(empresa.documento).includes(cnpj)) return false
      return true
    })

    const sort = this.sort
    return [...resultado].sort((a, b) => (sort === 'name' ? a.nome.localeCompare(b.nome, 'pt-BR') : 0))
  }

  get resultLabel(): string {
    return `${this.$t('dashboardEmpresas.lista.mostrando')} ${this.filtrados.length} ${this.$t('dashboardEmpresas.lista.de')} ${this.empresas.length}`
  }

  get summaryCards(): SummaryCardItem[] {
    return [
      { label: this.$t('dashboardEmpresas.resumo.total') as string, value: this.empresas.length, icon: 'mdi-domain', color: 'primary' },
      {
        label: this.$t('dashboardEmpresas.resumo.ativas') as string,
        value: this.empresas.filter((e) => e.status === 'ATIVA').length,
        icon: 'mdi-check-circle-outline',
        color: 'info',
      },
      {
        label: this.$t('dashboardEmpresas.resumo.clinicas') as string,
        value: this.empresas.filter((e) => e.tipo === 'CLINICA').length,
        icon: 'mdi-hospital-building',
        color: 'on-surface-variant',
      },
      {
        label: this.$t('dashboardEmpresas.resumo.individuais') as string,
        value: this.empresas.filter((e) => e.tipo === 'INDIVIDUAL').length,
        icon: 'mdi-account-outline',
        color: 'on-surface-variant',
      },
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

  rotulo(grupo: keyof EmpresaEnums, valor: string): string {
    return this.enums[grupo].find((opcao) => opcao.valor === valor)?.rotulo ?? valor
  }

  sincronizarDraftComAplicado() {
    this.draftNomeFantasia = this.filtros.value('q')
    this.draftRazaoSocial = this.filtros.value('razaoSocial')
    this.draftCnpj = this.filtros.value('cnpj')
  }

  async pesquisar() {
    await this.filtros.applyMany({
      q: this.draftNomeFantasia,
      razaoSocial: this.draftRazaoSocial,
      cnpj: this.draftCnpj,
    })
    this.sincronizarDraftComAplicado()
  }

  async limparNomeFantasia() {
    this.draftNomeFantasia = ''
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

  nova() {
    this.$router.push('/empresa/nova')
  }

  editar(id: number) {
    this.$router.push(`/empresa/${id}`)
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
}
</script>

<style scoped lang="scss">
.empresa-lista {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.empresa-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.empresa-lista__title {
  margin: 0;
  font-size: 1.7rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.empresa-lista__subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.empresa-lista__section {
  width: 100%;
}

.empresa-lista__table-card {
  border-radius: 12px;
  padding: 4px 12px;
}

.empresa-lista__nome-cell {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.empresa-lista__iniciais {
  font-size: 0.68rem;
  font-weight: 700;
}

.empresa-lista__nome-texto {
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
