<template>
  <v-card variant="flat" color="surface-variant" class="consulta-analytics">
    <v-card-title class="consulta-analytics__header">
      <span>{{ $t('home.analytics.tituloConsultas') }}</span>
      <NuvexaSelect
        :model-value="periodo"
        @update:model-value="periodo = $event"
        :items="periodoOpcoes"
        :label="$t('home.analytics.periodoLabel') as string"
        class="consulta-analytics__filtro"
      />
    </v-card-title>
    <v-card-text>
      <div v-if="loading" class="consulta-analytics__loading">
        <v-progress-circular indeterminate color="primary" />
      </div>

      <NuvexaEmptyState
        v-else-if="consultasJanela.length === 0"
        icon="mdi-chart-bar"
        :title="$t('home.analytics.vazioConsultas.titulo') as string"
        :description="$t('home.analytics.vazioConsultas.descricao') as string"
      />

      <div v-else class="consulta-analytics__grid">
        <div>
          <h3 class="consulta-analytics__subtitulo">{{ $t('home.analytics.consultasPorPeriodo') }}</h3>
          <NuvexaBarChart :rows="consultasPorPeriodoRows" />
        </div>
        <div>
          <h3 class="consulta-analytics__subtitulo">{{ $t('home.analytics.statusConsultas') }}</h3>
          <NuvexaBarChart :rows="statusRows" />
        </div>
        <div>
          <h3 class="consulta-analytics__subtitulo">{{ $t('home.analytics.distribuicaoPorTipo') }}</h3>
          <NuvexaBarChart :rows="tipoRows" />
        </div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaEmptyState from '../../../components/common/NuvexaEmptyState.vue'
import NuvexaBarChart from '../../../components/common/NuvexaBarChart.vue'
import NuvexaSelect from '../../../components/common/NuvexaSelect.vue'
import type { NuvexaBarChartRow } from '../../../components/common/NuvexaBarChart.vue'
import type { Consulta, ConsultaStatus, ConsultaTipo } from '../../../types/consulta'
import { consultaStatusColor } from '../../../util/consulta-status'

type Periodo = '7' | '30'

const STATUS_ORDEM: ConsultaStatus[] = ['REALIZADA', 'CONFIRMADA', 'AGENDADA', 'CANCELADA', 'FALTOU']
const TIPO_ORDEM: ConsultaTipo[] = ['PRIMEIRA_CONSULTA', 'RETORNO', 'AVALIACAO']

function isSameDay(isoDate: string, reference: Date): boolean {
  const date = new Date(isoDate)
  return (
    date.getFullYear() === reference.getFullYear() &&
    date.getMonth() === reference.getMonth() &&
    date.getDate() === reference.getDate()
  )
}

function formatWeekday(date: Date): string {
  const bruto = date.toLocaleDateString('pt-BR', { weekday: 'short' }).replace('.', '')
  return bruto.charAt(0).toUpperCase() + bruto.slice(1)
}

function formatDayMonth(date: Date): string {
  return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
}

@Component({ name: 'DashboardConsultaAnalytics', components: { NuvexaEmptyState, NuvexaBarChart, NuvexaSelect } })
export default class DashboardConsultaAnalytics extends Vue {
  @Prop({ required: true })
  consultas!: Consulta[]

  @Prop({ default: false })
  loading!: boolean

  periodo: Periodo = '7'

  get periodoOpcoes() {
    return [
      { value: '7', label: this.$t('filtroComum.periodo.last_7') as string },
      { value: '30', label: this.$t('filtroComum.periodo.last_30') as string },
    ]
  }

  get janelaDias(): number {
    return this.periodo === '7' ? 7 : 30
  }

  /**
   * Consultas dentro da janela selecionada (7 ou 30 dias corridos, incluindo hoje).
   */
  get consultasJanela(): Consulta[] {
    const limite = new Date()
    limite.setHours(0, 0, 0, 0)
    limite.setDate(limite.getDate() - this.janelaDias + 1)
    return this.consultas.filter((consulta) => new Date(consulta.dataHora) >= limite)
  }

  get consultasPorPeriodoRows(): NuvexaBarChartRow[] {
    return this.periodo === '7' ? this.bucketsDiarios() : this.bucketsSemanais()
  }

  /** Uma barra por dia dos últimos 7 dias, rotulada pelo dia da semana. */
  private bucketsDiarios(): NuvexaBarChartRow[] {
    const hoje = new Date()
    const buckets: NuvexaBarChartRow[] = []
    for (let i = 6; i >= 0; i--) {
      const dia = new Date(hoje)
      dia.setDate(dia.getDate() - i)
      const total = this.consultas.filter((consulta) => isSameDay(consulta.dataHora, dia)).length
      buckets.push({ label: formatWeekday(dia), value: total })
    }
    return buckets
  }

  /** Agrupa os últimos 30 dias em ~5 blocos semanais, rotulados pela data de início do bloco. */
  private bucketsSemanais(): NuvexaBarChartRow[] {
    const hoje = new Date()
    const numSemanas = Math.ceil(30 / 7)
    const buckets: NuvexaBarChartRow[] = []
    for (let i = numSemanas - 1; i >= 0; i--) {
      const inicio = new Date(hoje)
      inicio.setDate(inicio.getDate() - (i * 7 + 6))
      inicio.setHours(0, 0, 0, 0)
      const fim = new Date(hoje)
      fim.setDate(fim.getDate() - i * 7)
      fim.setHours(23, 59, 59, 999)
      const total = this.consultas.filter((consulta) => {
        const data = new Date(consulta.dataHora)
        return data >= inicio && data <= fim
      }).length
      buckets.push({ label: formatDayMonth(inicio), value: total })
    }
    return buckets
  }

  get statusRows(): NuvexaBarChartRow[] {
    return STATUS_ORDEM.map((status) => ({
      label: this.$t(`consulta.status.${status}`) as string,
      value: this.consultasJanela.filter((consulta) => consulta.status === status).length,
      color: consultaStatusColor(status),
    }))
  }

  get tipoRows(): NuvexaBarChartRow[] {
    return TIPO_ORDEM.map((tipo) => ({
      label: this.$t(`consulta.tipo.${tipo}`) as string,
      value: this.consultasJanela.filter((consulta) => consulta.tipo === tipo).length,
      color: 'secondary',
    }))
  }
}
</script>

<style scoped lang="scss">
.consulta-analytics {
  border-radius: 12px;
}

.consulta-analytics__header {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  font-size: 1.1rem;
  font-weight: 600;
}

.consulta-analytics__filtro {
  flex: 0 0 180px;
}

.consulta-analytics__loading {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

.consulta-analytics__grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 24px;

  @media (max-width: 1280px) {
    grid-template-columns: 1fr 1fr;
  }

  @media (max-width: 720px) {
    grid-template-columns: 1fr;
  }
}

.consulta-analytics__subtitulo {
  font-size: 0.9rem;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface-variant));
  margin-bottom: 8px;
}
</style>
