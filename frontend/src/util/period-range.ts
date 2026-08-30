import type { NuvexaPeriodPreset } from '../types/nuvexa-filters'

function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10)
}

export function todayIsoDate(): string {
  return toIsoDate(new Date())
}

export function subtractDays(isoDate: string, days: number): string {
  const date = new Date(isoDate)
  date.setDate(date.getDate() - days)
  return toIsoDate(date)
}

const DAYS_BACK_BY_PRESET: Record<NuvexaPeriodPreset, number> = {
  last_3: 2,
  last_7: 6,
  last_30: 29,
  last_90: 89,
  last_180: 179,
}

export function presetRange(preset: NuvexaPeriodPreset): { startDate: string; endDate: string } {
  const daysBack = DAYS_BACK_BY_PRESET[preset]
  return { startDate: subtractDays(todayIsoDate(), daysBack), endDate: todayIsoDate() }
}
