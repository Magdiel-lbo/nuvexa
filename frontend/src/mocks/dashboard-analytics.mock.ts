import type {
  DashboardCharts,
  DashboardFilters,
  DashboardOverview,
  DashboardTimeSeriesPoint,
} from '../types/dashboard-analytics'

function daysBetween(startDate: string, endDate: string): number {
  const ms = new Date(endDate).getTime() - new Date(startDate).getTime()
  return Math.max(1, Math.round(ms / (24 * 60 * 60 * 1000)) + 1)
}

function seededRandom(seed: number): () => number {
  let value = seed
  return () => {
    value = (value * 9301 + 49297) % 233280
    return value / 233280
  }
}

function buildTimeSeries(
  days: number,
  seed: number,
  base: number,
  spread: number,
  decimals = 0,
): DashboardTimeSeriesPoint[] {
  const random = seededRandom(seed)
  const points: DashboardTimeSeriesPoint[] = []
  const today = new Date()
  const factor = 10 ** decimals
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const value = Math.max(0, Math.round((base + (random() - 0.5) * spread) * factor) / factor)
    points.push({ date: date.toISOString().slice(0, 10), value })
  }
  return points
}

function activePatientsFactor(patientStatuses: DashboardFilters['patientStatuses']): number {
  const hasActive = patientStatuses.includes('ACTIVE')
  const hasInactive = patientStatuses.includes('INACTIVE')
  if (hasActive && !hasInactive) {
    return 0.82
  }
  if (hasInactive && !hasActive) {
    return 0.18
  }
  return 1
}

export function generateDashboardOverview(filters: DashboardFilters): DashboardOverview {
  const days = daysBetween(filters.startDate, filters.endDate)
  const random = seededRandom(days * 7)
  const patientsFactor = activePatientsFactor(filters.patientStatuses)

  const patients = Math.round((days * 4.2 + random() * 10) * patientsFactor)
  const appointments = Math.round(days * 1.4 + random() * 6)
  const conversionRate = Math.round((60 + random() * 20) * 10) / 10
  const satisfaction = Math.round((4.2 + random() * 0.6) * 10) / 10

  const hasComparison = filters.comparison !== 'none'
  const factor = filters.comparison === 'previous_year' ? 0.85 : 0.93

  return {
    patients: { value: patients, previousValue: hasComparison ? Math.round(patients * factor) : null },
    appointments: { value: appointments, previousValue: hasComparison ? Math.round(appointments * factor) : null },
    conversionRate: {
      value: conversionRate,
      previousValue: hasComparison ? Math.round(conversionRate * factor * 10) / 10 : null,
    },
    satisfaction: {
      value: satisfaction,
      previousValue: hasComparison ? Math.round(satisfaction * factor * 10) / 10 : null,
    },
  }
}

export function generateDashboardCharts(filters: DashboardFilters): DashboardCharts {
  const days = daysBetween(filters.startDate, filters.endDate)
  const patientsFactor = activePatientsFactor(filters.patientStatuses)

  return {
    patientsGrowth: buildTimeSeries(days, days * 3, 4, 5).map((point) => ({
      ...point,
      value: Math.round(point.value * patientsFactor),
    })),
    satisfactionTrend: buildTimeSeries(days, days * 5, 4.4, 0.8, 1).map((point) => ({
      ...point,
      value: Math.min(5, Math.max(3, point.value)),
    })),
    appointmentsByStatus: [
      { label: 'Realizadas', value: Math.round(days * 0.9 + 6) },
      { label: 'Agendadas', value: Math.round(days * 0.4 + 3) },
      { label: 'Canceladas', value: Math.round(days * 0.15 + 1) },
    ],
  }
}
