<template>
  <div class="consulta-lista">
    <div class="consulta-lista__header">
      <div>
        <h1 class="consulta-lista__title">{{ $t('dashboardConsultas.titulo') }}</h1>
        <p class="consulta-lista__subtitle">{{ $t('dashboardConsultas.subtitulo') }}</p>
      </div>
      <v-btn color="primary" prepend-icon="mdi-plus" size="large" @click="onCreate">
        {{ $t('dashboardConsultas.nova') }}
      </v-btn>
    </div>

    <NuvexaSummaryCards class="consulta-lista__section" :cards="summaryCards" />

    <ConsultaFilters
      class="consulta-lista__section"
      :search="search"
      :status="statusFilter"
      :type="typeFilter"
      @update:search="search = $event"
      @update:status="statusFilter = $event"
      @update:type="typeFilter = $event"
      @clear="clearFilters"
    />

    <v-card variant="flat" color="surface-variant" class="consulta-lista__section consulta-lista__table-card">
      <ConsultaTable
        v-if="filteredConsultas.length > 0"
        :consultas="filteredConsultas"
        @view="onView"
        @edit="onEdit"
        @cancel="onCancel"
        @delete="onDelete"
      />
      <NuvexaEmptyState
        v-else
        :icon="hasActiveFilters ? 'mdi-filter-remove-outline' : 'mdi-calendar-blank-outline'"
        :title="hasActiveFilters ? $t('dashboardConsultas.vazio.tituloFiltro') as string : $t('dashboardConsultas.vazio.titulo') as string"
        :description="hasActiveFilters ? $t('dashboardConsultas.vazio.descricaoFiltro') as string : $t('dashboardConsultas.vazio.descricao') as string"
        :action-label="hasActiveFilters ? null : ($t('dashboardConsultas.nova') as string)"
        :secondary-action-label="hasActiveFilters ? ($t('dashboardPacientes.filtros.limpar') as string) : null"
        @action="onCreate"
        @secondary-action="clearFilters"
      />
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import NuvexaSummaryCards from '../../components/common/NuvexaSummaryCards.vue'
import type { SummaryCardItem } from '../../components/common/NuvexaSummaryCards.vue'
import NuvexaEmptyState from '../../components/common/NuvexaEmptyState.vue'
import ConsultaFilters from './components/ConsultaFilters.vue'
import ConsultaTable from './components/ConsultaTable.vue'
import consultaService from '../../service/consulta-service'
import type { Consulta, ConsultaStatus, ConsultaTipo } from '../../types/consulta'
import { useAppStore } from '../../store/app.store'

function isSameDay(isoDate: string, reference: Date): boolean {
  const date = new Date(isoDate)
  return (
    date.getFullYear() === reference.getFullYear() &&
    date.getMonth() === reference.getMonth() &&
    date.getDate() === reference.getDate()
  )
}

@Component({
  name: 'ConsultaLista',
  components: { NuvexaSummaryCards, NuvexaEmptyState, ConsultaFilters, ConsultaTable },
})
export default class ConsultaLista extends Vue {
  consultas: Consulta[] = []

  search = ''
  statusFilter: ConsultaStatus | null = null
  typeFilter: ConsultaTipo | null = null

  get appStore() {
    return useAppStore()
  }

  async mounted() {
    await this.carregar()
  }

  async carregar() {
    this.consultas = await consultaService.listar()
  }

  get hasActiveFilters(): boolean {
    return !!this.search || !!this.statusFilter || !!this.typeFilter
  }

  get filteredConsultas(): Consulta[] {
    return this.consultas
      .filter((consulta) => {
        if (this.search && !consulta.pacienteNome.toLowerCase().includes(this.search.toLowerCase())) {
          return false
        }
        if (this.statusFilter && consulta.status !== this.statusFilter) {
          return false
        }
        if (this.typeFilter && consulta.tipo !== this.typeFilter) {
          return false
        }
        return true
      })
      .sort((a, b) => a.dataHora.localeCompare(b.dataHora))
  }

  get summary() {
    const now = new Date()
    return {
      total: this.consultas.length,
      hoje: this.consultas.filter((c) => isSameDay(c.dataHora, now)).length,
      confirmadas: this.consultas.filter((c) => c.status === 'CONFIRMADA').length,
      canceladas: this.consultas.filter((c) => c.status === 'CANCELADA' || c.status === 'FALTOU').length,
    }
  }

  get summaryCards(): SummaryCardItem[] {
    return [
      { label: this.$t('dashboardConsultas.resumo.total') as string, value: this.summary.total, icon: 'mdi-calendar-multiple', color: 'primary' },
      { label: this.$t('dashboardConsultas.resumo.hoje') as string, value: this.summary.hoje, icon: 'mdi-calendar-today', color: 'secondary' },
      { label: this.$t('dashboardConsultas.resumo.confirmadas') as string, value: this.summary.confirmadas, icon: 'mdi-calendar-check', color: 'success' },
      { label: this.$t('dashboardConsultas.resumo.canceladas') as string, value: this.summary.canceladas, icon: 'mdi-calendar-remove', color: 'error' },
    ]
  }

  clearFilters() {
    this.search = ''
    this.statusFilter = null
    this.typeFilter = null
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
    await consultaService.cancelar(consulta)
    await this.carregar()
    this.appStore.setToast({ mensagem: `Consulta de ${consulta.pacienteNome} cancelada.`, erro: false })
  }

  async onDelete(consulta: Consulta) {
    await consultaService.excluir(consulta.id)
    await this.carregar()
    this.appStore.setToast({ mensagem: `Consulta de ${consulta.pacienteNome} removida.`, erro: false })
  }
}
</script>

<style scoped lang="scss">
.consulta-lista__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.consulta-lista__title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 4px;
}

.consulta-lista__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
  max-width: 60ch;
}

.consulta-lista__section {
  margin-bottom: 20px;
}

.consulta-lista__table-card {
  border-radius: 12px;
}
</style>
