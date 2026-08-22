export type NuvexaPeriodPreset = 'today' | 'yesterday' | 'last_3' | 'last_7' | 'last_30' | 'last_90' | 'custom'

export interface NuvexaPeriodValue {
  preset: NuvexaPeriodPreset
  startDate: string
  endDate: string
}

export type NuvexaStatusValue = 'ACTIVE' | 'INACTIVE' | null

export type NuvexaYesNoValue = boolean | null
