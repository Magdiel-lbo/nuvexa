<template>
  <v-card variant="flat" color="surface-variant" class="chart-card">
    <v-card-text>
      <div class="chart-card__title">{{ title }}</div>
      <div class="chart-card__canvas">
        <Bar :key="themeStore.name" :data="chartData" :options="chartOptions" />
      </div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-facing-decorator'
import { Bar } from 'vue-chartjs'
import type { ChartData, ChartOptions } from 'chart.js'
import { useThemeStore } from '../../../store/theme.store'
import { getChartTheme } from '../../../util/chart-theme'

@Component({ name: 'DashboardBarChart', components: { Bar } })
export default class DashboardBarChart extends Vue {
  @Prop({ required: true })
  title!: string

  @Prop({ required: true })
  labels!: string[]

  @Prop({ required: true })
  values!: number[]

  get themeStore() {
    return useThemeStore()
  }

  get chartData(): ChartData<'bar'> {
    const theme = getChartTheme()
    const colors = [theme.primary, theme.secondary, theme.warning, theme.success]
    return {
      labels: this.labels,
      datasets: [
        {
          data: this.values,
          backgroundColor: this.labels.map((_, index) => colors[index % colors.length]),
          borderRadius: 6,
          maxBarThickness: 48,
        },
      ],
    }
  }

  get chartOptions(): ChartOptions<'bar'> {
    const theme = getChartTheme()
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {},
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: theme.textColor },
        },
        y: {
          grid: { color: `${theme.gridColor}55` },
          ticks: { color: theme.textColor, precision: 0 },
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
