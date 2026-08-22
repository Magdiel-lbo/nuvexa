<template>
  <div class="analytics-dashboard">
    <div class="analytics-dashboard__header">
      <h1 class="analytics-dashboard__title">{{ $t('dashboardAnalytics.titulo') }}</h1>
      <p class="analytics-dashboard__subtitle">{{ $t('dashboardAnalytics.subtitulo') }}</p>
    </div>

    <DashboardFiltersBar />

    <DashboardIndicatorCards :overview="overview" :comparison="filtersStore.comparison" />

    <div class="analytics-dashboard__charts">
      <DashboardLineChart
        class="analytics-dashboard__chart-primary"
        :title="$t('dashboardAnalytics.graficos.crescimentoPacientes') as string"
        :labels="patientsGrowthLabels"
        :values="patientsGrowthValues"
      />

      <DashboardBarChart
        class="analytics-dashboard__chart-secondary"
        :title="$t('dashboardAnalytics.graficos.consultasPorStatus') as string"
        :labels="appointmentsStatusLabels"
        :values="appointmentsStatusValues"
      />

      <DashboardLineChart
        class="analytics-dashboard__chart-full"
        :title="$t('dashboardAnalytics.graficos.tendenciaSatisfacao') as string"
        :labels="satisfactionTrendLabels"
        :values="satisfactionTrendValues"
        value-suffix="/5"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-facing-decorator'
import DashboardFiltersBar from './components/DashboardFiltersBar.vue'
import DashboardIndicatorCards from './components/DashboardIndicatorCards.vue'
import DashboardLineChart from './components/DashboardLineChart.vue'
import DashboardBarChart from './components/DashboardBarChart.vue'
import dashboardService from '../../service/dashboard-service'
import { useDashboardFiltersStore } from '../../store/dashboard-filters.store'
import type { DashboardCharts, DashboardOverview } from '../../types/dashboard-analytics'

function formatDayLabel(isoDate: string): string {
  const date = new Date(isoDate)
  return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
}

@Component({
  name: 'Dashboard',
  components: { DashboardFiltersBar, DashboardIndicatorCards, DashboardLineChart, DashboardBarChart },
})
export default class Dashboard extends Vue {
  overview: DashboardOverview | null = null
  charts: DashboardCharts | null = null

  get filtersStore() {
    return useDashboardFiltersStore()
  }

  get preset() {
    return this.filtersStore.preset
  }

  get startDate() {
    return this.filtersStore.startDate
  }

  get endDate() {
    return this.filtersStore.endDate
  }

  get comparison() {
    return this.filtersStore.comparison
  }

  get onlyActivePatients() {
    return this.filtersStore.onlyActivePatients
  }

  get patientsGrowthLabels(): string[] {
    return this.charts?.patientsGrowth.map((point) => formatDayLabel(point.date)) ?? []
  }

  get patientsGrowthValues(): number[] {
    return this.charts?.patientsGrowth.map((point) => point.value) ?? []
  }

  get satisfactionTrendLabels(): string[] {
    return this.charts?.satisfactionTrend.map((point) => formatDayLabel(point.date)) ?? []
  }

  get satisfactionTrendValues(): number[] {
    return this.charts?.satisfactionTrend.map((point) => point.value) ?? []
  }

  get appointmentsStatusLabels(): string[] {
    return this.charts?.appointmentsByStatus.map((item) => item.label) ?? []
  }

  get appointmentsStatusValues(): number[] {
    return this.charts?.appointmentsByStatus.map((item) => item.value) ?? []
  }

  mounted() {
    this.loadData()
  }

  @Watch('preset')
  @Watch('startDate')
  @Watch('endDate')
  @Watch('comparison')
  @Watch('onlyActivePatients')
  onFiltersChanged() {
    this.loadData()
  }

  async loadData() {
    const filters = {
      preset: this.filtersStore.preset,
      startDate: this.filtersStore.startDate,
      endDate: this.filtersStore.endDate,
      comparison: this.filtersStore.comparison,
      onlyActivePatients: this.filtersStore.onlyActivePatients,
    }
    const [overview, charts] = await Promise.all([
      dashboardService.getOverview(filters),
      dashboardService.getCharts(filters),
    ])
    this.overview = overview
    this.charts = charts
  }
}
</script>

<style scoped lang="scss">
.analytics-dashboard__header {
  margin-bottom: 24px;
}

.analytics-dashboard__title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 4px;
}

.analytics-dashboard__subtitle {
  color: rgb(var(--v-theme-on-surface-variant));
  margin: 0;
  max-width: 60ch;
}

.analytics-dashboard__charts {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;

  @media (max-width: 960px) {
    grid-template-columns: 1fr;
  }
}

.analytics-dashboard__chart-full {
  grid-column: 1 / -1;
}
</style>
