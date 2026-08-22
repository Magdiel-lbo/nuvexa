<template>
  <div class="indicator-grid">
    <v-card v-for="item in cards" :key="item.label" variant="flat" color="surface-variant" class="indicator-card">
      <v-card-text>
        <div class="indicator-card__label">{{ item.label }}</div>
        <div class="indicator-card__value">{{ item.formattedValue }}</div>
        <div v-if="item.delta !== null" class="indicator-card__delta" :class="item.deltaColor">
          <v-icon :icon="item.deltaIcon" size="16" />
          {{ item.deltaLabel }}
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import type { DashboardComparison, DashboardOverview } from '../../../types/dashboard-analytics'

interface IndicatorCard {
  label: string
  formattedValue: string
  delta: number | null
  deltaLabel: string
  deltaIcon: string
  deltaColor: string
}

@Component({ name: 'DashboardIndicatorCards' })
export default class DashboardIndicatorCards extends Vue {
  @Prop({ default: null })
  overview!: DashboardOverview | null

  @Prop({ required: true })
  comparison!: DashboardComparison

  deltaOf(value: number, previousValue: number | null): number | null {
    if (previousValue === null || previousValue === 0) {
      return null
    }
    return Math.round(((value - previousValue) / previousValue) * 1000) / 10
  }

  buildCard(label: string, value: number, previousValue: number | null, formattedValue: string): IndicatorCard {
    const delta = this.deltaOf(value, previousValue)
    return {
      label,
      formattedValue,
      delta,
      deltaLabel: delta === null ? '' : `${delta > 0 ? '+' : ''}${delta.toLocaleString('pt-BR')}%`,
      deltaIcon: delta !== null && delta < 0 ? 'mdi-arrow-down' : 'mdi-arrow-up',
      deltaColor: delta !== null && delta < 0 ? 'text-error' : 'text-success',
    }
  }

  get cards(): IndicatorCard[] {
    if (!this.overview) {
      return []
    }

    return [
      this.buildCard(
        this.$t('dashboardAnalytics.indicadores.pacientes') as string,
        this.overview.patients.value,
        this.overview.patients.previousValue,
        this.overview.patients.value.toLocaleString('pt-BR'),
      ),
      this.buildCard(
        this.$t('dashboardAnalytics.indicadores.consultas') as string,
        this.overview.appointments.value,
        this.overview.appointments.previousValue,
        this.overview.appointments.value.toLocaleString('pt-BR'),
      ),
      this.buildCard(
        this.$t('dashboardAnalytics.indicadores.conversao') as string,
        this.overview.conversionRate.value,
        this.overview.conversionRate.previousValue,
        `${this.overview.conversionRate.value.toLocaleString('pt-BR')}%`,
      ),
      this.buildCard(
        this.$t('dashboardAnalytics.indicadores.satisfacao') as string,
        this.overview.satisfaction.value,
        this.overview.satisfaction.previousValue,
        `${this.overview.satisfaction.value.toLocaleString('pt-BR')}/5`,
      ),
    ]
  }
}
</script>

<style scoped lang="scss">
.indicator-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 960px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.indicator-card {
  border-radius: 12px;
}

.indicator-card__label {
  font-size: 0.85rem;
  color: rgb(var(--v-theme-on-surface-variant));
  margin-bottom: 4px;
}

.indicator-card__value {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1.2;
}

.indicator-card__delta {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 0.8rem;
  font-weight: 600;
  margin-top: 6px;
}
</style>
