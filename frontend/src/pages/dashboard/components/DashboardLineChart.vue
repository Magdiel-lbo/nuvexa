<template>
  <v-card variant="flat" color="surface-variant" class="chart-card">
    <v-card-text>
      <div class="chart-card__title">{{ title }}</div>
      <div class="chart-card__canvas">
        <Line :key="themeStore.name" :data="chartData" :options="chartOptions" />
      </div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import { Line } from 'vue-chartjs'
import type { ChartData, ChartOptions } from 'chart.js'
import { useThemeStore } from '../../../store/theme.store'
import { getChartTheme } from '../../../util/chart-theme'

@Component({ name: 'DashboardLineChart', components: { Line } })
export default class DashboardLineChart extends Vue {
  @Prop({ required: true })
  title!: string

  @Prop({ required: true })
  labels!: string[]

  @Prop({ required: true })
  values!: number[]

  @Prop({ default: null })
  valueSuffix!: string | null

  get themeStore() {
    return useThemeStore()
  }

  get chartData(): ChartData<'line'> {
    const theme = getChartTheme()
    return {
      labels: this.labels,
      datasets: [
        {
          data: this.values,
          borderColor: theme.primary,
          backgroundColor: `${theme.primary}33`,
          fill: true,
          tension: 0.35,
          pointRadius: 0,
          pointHoverRadius: 4,
          borderWidth: 2,
        },
      ],
    }
  }

  get chartOptions(): ChartOptions<'line'> {
    const theme = getChartTheme()
    return {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (context) => `${context.formattedValue}${this.valueSuffix ?? ''}`,
          },
        },
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: theme.textColor, maxRotation: 0, autoSkip: true, maxTicksLimit: 6 },
        },
        y: {
          grid: { color: `${theme.gridColor}55` },
          ticks: { color: theme.textColor },
        },
      },
    }
  }
}
</script>

<style scoped lang="scss">
.chart-card {
  border-radius: 12px;
  height: 100%;
}

.chart-card__title {
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 12px;
}

.chart-card__canvas {
  height: 260px;
  position: relative;
}
</style>
