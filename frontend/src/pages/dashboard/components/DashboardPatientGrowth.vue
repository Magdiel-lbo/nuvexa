<template>
  <v-card variant="flat" color="surface-variant" class="patient-growth">
    <v-card-title class="patient-growth__header">
      <span>{{ $t('home.analytics.crescimentoPacientes') }}</span>
      <NuvexaSelect
        :model-value="periodo"
        @update:model-value="periodo = $event"
        :items="periodoOpcoes"
        :label="$t('home.analytics.periodoLabel') as string"
        class="patient-growth__filtro"
      />
    </v-card-title>
    <v-card-text>
      <div v-if="loading" class="patient-growth__loading">
        <v-progress-circular indeterminate color="primary" />
      </div>

      <NuvexaEmptyState
        v-else-if="pacientes.length === 0"
        icon="mdi-account-multiple-plus-outline"
        :title="$t('home.analytics.vazioCrescimento.titulo') as string"
        :description="$t('home.analytics.vazioCrescimento.descricao') as string"
      />

      <template v-else>
        <div class="patient-growth__resumo">
          <span class="patient-growth__valor">{{ novosNaJanela }}</span>
          <span class="patient-growth__label">{{ $t('home.analytics.novosNoPeriodo') }} · {{ periodoLabel }}</span>
          <span
            v-if="variacaoPercentual !== null"
            class="patient-growth__variacao"
            :class="variacaoPercentual >= 0 ? 'text-success' : 'text-error'"
          >
            {{ variacaoPercentual >= 0 ? '+' : '' }}{{ variacaoPercentual }}% {{ $t('home.analytics.vsPeriodoAnterior') }}
          </span>
        </div>
        <NuvexaLineChart :points="pontos" />
      </template>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaEmptyState from '../../../components/common/NuvexaEmptyState.vue'
import NuvexaLineChart from '../../../components/common/NuvexaLineChart.vue'
import NuvexaSelect from '../../../components/common/NuvexaSelect.vue'
import type { NuvexaLineChartPoint } from '../../../components/common/NuvexaLineChart.vue'
import type { PacienteResponse } from '../../../types/paciente'

type Periodo = '30' | '180'

function isSameDay(isoDate: string, reference: Date): boolean {
  const date = new Date(isoDate)
  return (
    date.getFullYear() === reference.getFullYear() &&
    date.getMonth() === reference.getMonth() &&
    date.getDate() === reference.getDate()
  )
}

function formatDayMonth(date: Date): string {
  return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
}

function formatMonthAbbrev(date: Date): string {
  const bruto = date.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')
  return bruto.charAt(0).toUpperCase() + bruto.slice(1)
}

@Component({ name: 'DashboardPatientGrowth', components: { NuvexaEmptyState, NuvexaLineChart, NuvexaSelect } })
export default class DashboardPatientGrowth extends Vue {
  @Prop({ required: true })
  pacientes!: PacienteResponse[]

  @Prop({ default: false })
  loading!: boolean

  periodo: Periodo = '30'

  get periodoOpcoes() {
    return [
      { value: '30', label: this.$t('filtroComum.periodo.last_30') as string },
      { value: '180', label: this.$t('filtroComum.periodo.last_180') as string },
    ]
  }

  get periodoLabel(): string {
    return this.periodoOpcoes.find((opcao) => opcao.value === this.periodo)?.label ?? ''
  }

  get janelaDias(): number {
    return this.periodo === '30' ? 30 : 180
  }

  get novosNaJanela(): number {
    return this.contarNoIntervalo(this.janelaDias, 0)
  }

  get novosNaJanelaAnterior(): number {
    return this.contarNoIntervalo(this.janelaDias, this.janelaDias)
  }

  /** Conta pacientes criados no intervalo [hoje - deslocamento - dias, hoje - deslocamento). */
  private contarNoIntervalo(dias: number, deslocamento: number): number {
    const fim = new Date()
    fim.setDate(fim.getDate() - deslocamento)
    const inicio = new Date(fim)
    inicio.setDate(inicio.getDate() - dias)
    return this.pacientes.filter((paciente) => {
      const criadoEm = new Date(paciente.criadoEm)
      return criadoEm >= inicio && criadoEm < fim
    }).length
  }

  /** Só mostra a comparação se o período anterior tiver dado real — nunca "N/A" ou "0%" inventado. */
  get variacaoPercentual(): number | null {
    if (this.novosNaJanelaAnterior === 0) {
      return null
    }
    return Math.round(((this.novosNaJanela - this.novosNaJanelaAnterior) / this.novosNaJanelaAnterior) * 100)
  }

  get pontos(): NuvexaLineChartPoint[] {
    return this.periodo === '30' ? this.pontosSemanais() : this.pontosMensais()
  }

  /** Últimos 30 dias agrupados em ~5 blocos semanais, mesma lógica de bucket de DashboardConsultaAnalytics. */
  private pontosSemanais(): NuvexaLineChartPoint[] {
    const hoje = new Date()
    const numSemanas = Math.ceil(30 / 7)
    const pontos: NuvexaLineChartPoint[] = []
    for (let i = numSemanas - 1; i >= 0; i--) {
      const inicio = new Date(hoje)
      inicio.setDate(inicio.getDate() - (i * 7 + 6))
      inicio.setHours(0, 0, 0, 0)
      const fim = new Date(hoje)
      fim.setDate(fim.getDate() - i * 7)
      fim.setHours(23, 59, 59, 999)
      const total = this.pacientes.filter((paciente) => {
        const criadoEm = new Date(paciente.criadoEm)
        return criadoEm >= inicio && criadoEm <= fim
      }).length
      pontos.push({ label: formatDayMonth(inicio), value: total })
    }
    return pontos
  }

  /** Últimos 6 meses agrupados por mês calendário, rotulados pelo nome abreviado do mês. */
  private pontosMensais(): NuvexaLineChartPoint[] {
    const hoje = new Date()
    const pontos: NuvexaLineChartPoint[] = []
    for (let i = 5; i >= 0; i--) {
      const mesRef = new Date(hoje.getFullYear(), hoje.getMonth() - i, 1)
      const total = this.pacientes.filter((paciente) => {
        const criadoEm = new Date(paciente.criadoEm)
        return criadoEm.getFullYear() === mesRef.getFullYear() && criadoEm.getMonth() === mesRef.getMonth()
      }).length
      pontos.push({ label: formatMonthAbbrev(mesRef), value: total })
    }
    return pontos
  }
}
</script>

<style scoped lang="scss">
.patient-growth {
  border-radius: 12px;
  height: 100%;
}

.patient-growth__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  font-size: 1.1rem;
  font-weight: 600;
}

.patient-growth__filtro {
  flex: 0 0 180px;
}

.patient-growth__loading {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.patient-growth__resumo {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.patient-growth__valor {
  font-size: 1.75rem;
  font-weight: 700;
}

.patient-growth__label {
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
}

.patient-growth__variacao {
  font-size: 0.85rem;
  font-weight: 600;
}
</style>
