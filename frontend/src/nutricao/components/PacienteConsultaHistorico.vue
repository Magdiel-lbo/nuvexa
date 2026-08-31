<template>
  <div class="paciente-consulta-historico">
    <button
      type="button"
      class="paciente-consulta-historico__header"
      :aria-expanded="expanded"
      @click="expanded = !expanded"
    >
      <span class="paciente-consulta-historico__title">
        {{ $t('paciente.detalhe.historicoConsultas') }}
        <span class="paciente-consulta-historico__count">({{ historico.length }})</span>
      </span>
      <v-icon :icon="expanded ? 'mdi-chevron-up' : 'mdi-chevron-down'" />
    </button>

    <div v-if="expanded" class="paciente-consulta-historico__body">
      <v-data-table
        v-if="historico.length > 0"
        :headers="headers"
        :items="historico"
        :items-per-page="5"
        mobile-breakpoint="sm"
      >
        <template #item.dataHora="{ item }">{{ formatDateTime(item.dataHora) }}</template>
        <template #item.tipo="{ item }">
          <v-chip size="small" variant="tonal" color="secondary">{{ $t(`consulta.tipo.${item.tipo}`) }}</v-chip>
        </template>
        <template #item.status="{ item }">
          <v-chip size="small" variant="tonal" :color="statusColor(item.status)">
            {{ $t(`consulta.status.${item.status}`) }}
          </v-chip>
        </template>
      </v-data-table>

      <NuvexaEmptyState
        v-else
        icon="mdi-calendar-blank-outline"
        :title="$t('paciente.detalhe.nenhumaConsultaTitulo') as string"
        :description="$t('paciente.detalhe.nenhumaConsultaDescricao') as string"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-facing-decorator'
import NuvexaEmptyState from '../../components/common/NuvexaEmptyState.vue'
import type { Consulta, ConsultaStatus } from '../../types/consulta'
import consultaService from '../../service/consulta-service'
import { consultaStatusColor } from '../../util/consulta-status'

@Component({ name: 'PacienteConsultaHistorico', components: { NuvexaEmptyState } })
export default class PacienteConsultaHistorico extends Vue {
  @Prop({ required: true })
  pacienteId!: number

  expanded = false
  historico: Consulta[] = []

  get headers() {
    return [
      { title: this.$t('dashboardConsultas.tabela.dataHora'), key: 'dataHora' },
      { title: this.$t('dashboardConsultas.filtros.tipo'), key: 'tipo' },
      { title: this.$t('dashboardConsultas.filtros.status'), key: 'status' },
    ]
  }

  async mounted() {
    await this.carregar()
  }

  @Watch('pacienteId')
  async onPacienteIdChange() {
    await this.carregar()
  }

  async carregar() {
    this.historico = await consultaService.buscarPorPaciente(this.pacienteId)
  }

  statusColor(status: ConsultaStatus): string {
    return consultaStatusColor(status)
  }

  formatDateTime(iso: string): string {
    const date = new Date(iso)
    const dateLabel = date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' })
    const timeLabel = date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    return `${dateLabel} · ${timeLabel}`
  }
}
</script>

<style scoped lang="scss">
.paciente-consulta-historico {
  margin-top: 24px;
}

.paciente-consulta-historico__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 12px 0;
  background: none;
  border: none;
  border-bottom: 1px solid rgba(var(--v-theme-on-surface), 0.12);
  cursor: pointer;
  font: inherit;
  color: inherit;
  text-align: left;
}

.paciente-consulta-historico__title {
  font-size: 1.15rem;
  font-weight: 600;
}

.paciente-consulta-historico__count {
  color: rgb(var(--v-theme-on-surface-variant));
  font-weight: 400;
}

.paciente-consulta-historico__body {
  margin-top: 16px;
}
</style>
