<template>
  <div class="patient-indicators">
    <v-card variant="flat" color="surface-variant" class="patient-indicators__card">
      <v-card-title class="patient-indicators__title">{{ $t('home.indicadores.porObjetivo') }}</v-card-title>
      <v-card-text>
        <div v-if="loading" class="patient-indicators__loading">
          <v-progress-circular indeterminate color="primary" />
        </div>

        <NuvexaEmptyState
          v-else-if="linhas.length === 0"
          icon="mdi-account-group-outline"
          :title="$t('home.indicadores.vazio.titulo') as string"
          :description="$t('home.indicadores.vazio.descricao') as string"
          :action-label="$t('home.indicadores.vazio.cta') as string"
          @action="novoPaciente"
        />

        <NuvexaDonutChart v-else :slices="distribuicaoObjetivo" />
      </v-card-text>
    </v-card>

    <v-card variant="flat" color="surface-variant" class="patient-indicators__card">
      <v-card-title class="patient-indicators__title">{{ $t('home.indicadores.porImc') }}</v-card-title>
      <v-card-text>
        <div v-if="loading" class="patient-indicators__loading">
          <v-progress-circular indeterminate color="primary" />
        </div>

        <NuvexaEmptyState
          v-else-if="linhas.length === 0"
          icon="mdi-scale-bathroom"
          :title="$t('home.indicadores.vazio.titulo') as string"
          :description="$t('home.indicadores.vazio.descricao') as string"
          :action-label="$t('home.indicadores.vazio.cta') as string"
          @action="novoPaciente"
        />

        <NuvexaBarChart v-else :rows="distribuicaoImc" />
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import NuvexaEmptyState from '../../../components/common/NuvexaEmptyState.vue'
import NuvexaBarChart from '../../../components/common/NuvexaBarChart.vue'
import NuvexaDonutChart from '../../../components/common/NuvexaDonutChart.vue'
import type { NuvexaBarChartRow } from '../../../components/common/NuvexaBarChart.vue'
import type { NuvexaDonutChartSlice } from '../../../components/common/NuvexaDonutChart.vue'
import { carregarRotulosEnum } from '../../../nutricao/utils/enum-rotulos'
import type { PacienteRelatorioLinha } from '../../../nutricao/types/paciente-relatorio'

function agrupar(valores: string[]): NuvexaBarChartRow[] {
  const contagem = new Map<string, number>()
  for (const valor of valores) {
    contagem.set(valor, (contagem.get(valor) ?? 0) + 1)
  }
  return [...contagem.entries()].map(([label, value]) => ({ label, value })).sort((a, b) => b.value - a.value)
}

@Component({ name: 'DashboardPatientIndicators', components: { NuvexaEmptyState, NuvexaBarChart, NuvexaDonutChart } })
export default class DashboardPatientIndicators extends Vue {
  @Prop({ required: true })
  linhas!: PacienteRelatorioLinha[]

  @Prop({ default: false })
  loading!: boolean

  rotulosObjetivo: Record<string, string> = {}

  async mounted() {
    this.rotulosObjetivo = await carregarRotulosEnum()
  }

  get distribuicaoObjetivo(): NuvexaDonutChartSlice[] {
    return agrupar(this.linhas.map((linha) => this.rotulosObjetivo[linha.objetivo] ?? linha.objetivo))
  }

  get distribuicaoImc(): NuvexaBarChartRow[] {
    return agrupar(this.linhas.map((linha) => linha.classificacaoImc))
  }

  novoPaciente() {
    this.$router.push('/pacientes/novo')
  }
}
</script>

<style scoped lang="scss">
.patient-indicators {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.patient-indicators__card {
  border-radius: 12px;
}

.patient-indicators__title {
  font-size: 1.1rem;
  font-weight: 600;
}

.patient-indicators__loading {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}
</style>
