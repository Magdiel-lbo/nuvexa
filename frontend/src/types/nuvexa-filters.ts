export type NuvexaPeriodPreset = 'last_3' | 'last_7' | 'last_30' | 'last_90' | 'last_180'

export interface NuvexaPeriodValue {
  preset: NuvexaPeriodPreset
  startDate: string
  endDate: string
}

export type NuvexaStatusValue = 'ACTIVE' | 'INACTIVE' | null

export type NuvexaStatusMultiValue = ('ACTIVE' | 'INACTIVE')[]
