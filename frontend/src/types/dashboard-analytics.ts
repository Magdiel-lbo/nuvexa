import type { NuvexaPeriodValue, NuvexaYesNoValue } from './nuvexa-filters'

export type DashboardComparison = 'none' | 'previous_period' | 'previous_year'

export interface DashboardFilters extends NuvexaPeriodValue {
  comparison: DashboardComparison
  onlyActivePatients: NuvexaYesNoValue
}

export interface DashboardIndicator {
  value: number
  previousValue: number | null
}

export interface DashboardOverview {
  patients: DashboardIndicator
  appointments: DashboardIndicator
  conversionRate: DashboardIndicator
  satisfaction: DashboardIndicator
}

export interface DashboardTimeSeriesPoint {
  date: string
  value: number
}

export interface DashboardStatusBreakdown {
  label: string
  value: number
}

export interface DashboardCharts {
  patientsGrowth: DashboardTimeSeriesPoint[]
  appointmentsByStatus: DashboardStatusBreakdown[]
  satisfactionTrend: DashboardTimeSeriesPoint[]
}
