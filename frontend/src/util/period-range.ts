import type { NuvexaPeriodPreset } from '../types/nuvexa-filters'

export const MAX_CUSTOM_RANGE_DAYS = 180

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

export function daysBetweenInclusive(startDate: string, endDate: string): number {
  const ms = new Date(endDate).getTime() - new Date(startDate).getTime()
  return Math.round(ms / (24 * 60 * 60 * 1000)) + 1
}

const DAYS_BACK_BY_PRESET: Record<Exclude<NuvexaPeriodPreset, 'custom'>, number> = {
  today: 0,
  yesterday: 1,
  last_3: 2,
  last_7: 6,
  last_30: 29,
  last_90: 89,
}

export function presetRange(preset: Exclude<NuvexaPeriodPreset, 'custom'>): { startDate: string; endDate: string } {
  const daysBack = DAYS_BACK_BY_PRESET[preset]
  if (preset === 'yesterday') {
    const yesterday = subtractDays(todayIsoDate(), 1)
    return { startDate: yesterday, endDate: yesterday }
  }
  return { startDate: subtractDays(todayIsoDate(), daysBack), endDate: todayIsoDate() }
}
