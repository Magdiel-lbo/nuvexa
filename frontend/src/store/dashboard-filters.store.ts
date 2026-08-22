import { defineStore } from 'pinia'
import { presetRange } from '../util/period-range'
import type { DashboardComparison, DashboardFilters } from '../types/dashboard-analytics'
import type { NuvexaPeriodValue } from '../types/nuvexa-filters'

export const useDashboardFiltersStore = defineStore('dashboardFilters', {
  state: (): DashboardFilters => ({
    preset: 'last_30',
    comparison: 'previous_period',
    onlyActivePatients: null,
    ...presetRange('last_30'),
  }),
  actions: {
    setPeriod(period: NuvexaPeriodValue) {
      this.preset = period.preset
      this.startDate = period.startDate
      this.endDate = period.endDate
    },
    setComparison(comparison: DashboardComparison) {
      this.comparison = comparison
    },
    setOnlyActivePatients(onlyActivePatients: DashboardFilters['onlyActivePatients']) {
      this.onlyActivePatients = onlyActivePatients
    },
  },
})
