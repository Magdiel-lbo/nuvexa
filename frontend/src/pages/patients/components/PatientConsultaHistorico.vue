<template>
  <div class="patient-consulta-historico">
    <button
      type="button"
      class="patient-consulta-historico__header"
      :aria-expanded="expanded"
      @click="expanded = !expanded"
    >
      <span class="patient-consulta-historico__title">
        {{ $t('paciente.detalhe.historicoConsultas') }}
        <span class="patient-consulta-historico__count">({{ historico.length }})</span>
      </span>
      <v-icon :icon="expanded ? 'mdi-chevron-up' : 'mdi-chevron-down'" />
    </button>

    <div v-if="expanded" class="patient-consulta-historico__body">
      <v-data-table
        v-if="historico.length > 0"
        :headers="headers"
        :items="historico"
        :items-per-page="5"
        mobile-breakpoint="sm"
      >
        <template #item.date="{ item }">{{ formatDateTime(item.date) }}</template>
        <template #item.type="{ item }">
          <v-chip size="small" variant="tonal" color="secondary">{{ $t(`consulta.tipo.${item.type}`) }}</v-chip>
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
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaEmptyState from '../../../components/common/NuvexaEmptyState.vue'
import type { Consulta, ConsultaStatus } from '../../../types/consulta'
import { mockConsultas } from '../../../mocks/consultas.mock'
import { consultaStatusColor } from '../../../util/consulta-status'

@Component({ name: 'PatientConsultaHistorico', components: { NuvexaEmptyState } })
export default class PatientConsultaHistorico extends Vue {
  @Prop({ required: true })
  patientId!: number

  expanded = false

  get headers() {
    return [
      { title: this.$t('dashboardConsultas.tabela.dataHora'), key: 'date' },
      { title: this.$t('dashboardConsultas.filtros.tipo'), key: 'type' },
      { title: this.$t('dashboardConsultas.filtros.status'), key: 'status' },
    ]
  }

  get historico(): Consulta[] {
    return mockConsultas
      .filter((consulta) => consulta.patientId === this.patientId)
      .sort((a, b) => b.date.localeCompare(a.date))
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
.patient-consulta-historico {
  margin-top: 24px;
}

.patient-consulta-historico__header {
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

.patient-consulta-historico__title {
  font-size: 1.15rem;
  font-weight: 600;
}

.patient-consulta-historico__count {
  color: rgb(var(--v-theme-on-surface-variant));
  font-weight: 400;
}

.patient-consulta-historico__body {
  margin-top: 16px;
}
</style>
