export type Gender = 'MALE' | 'FEMALE'

export type Goal =
  | 'LOSE_WEIGHT'
  | 'MAINTAIN_WEIGHT'
  | 'GAIN_MUSCLE_MASS'
  | 'IMPROVE_CONDITIONING'

export type ActivityLevel =
  | 'SEDENTARY'
  | 'LIGHTLY_ACTIVE'
  | 'MODERATELY_ACTIVE'
  | 'VERY_ACTIVE'
  | 'EXTRA_ACTIVE'

export interface PatientResponse {
  id: number
  name: string
  birthDate: string
  gender: Gender
  height: number
  weight: number
  goal: Goal
  activityLevel: ActivityLevel
  manualDailyCalories: number | null
  notes: string | null
  createdAt: string
  updatedAt: string
  age: number
  bmi: number
  bmiClassification: string
  bmr: number
  dailyCalorieExpenditure: number
}

export interface PatientCreateRequest {
  name: string
  birthDate: string
  gender: Gender
  height: number
  weight: number
  goal: Goal
  activityLevel: ActivityLevel
  manualDailyCalories?: number | null
  notes?: string | null
}

export type PatientUpdateRequest = PatientCreateRequest

export interface EnumOption {
  value: string
  label: string
}

export interface PatientEnumsResponse {
  genders: EnumOption[]
  goals: EnumOption[]
  activityLevels: EnumOption[]
}

export interface ReportColumn {
  key: string
  label: string
  order: number
}

export interface PatientReportRow {
  id: number
  name: string
  age: number
  gender: Gender
  goal: Goal
  activityLevel: ActivityLevel
  bmi: number
  bmiClassification: string
  dailyCalorieExpenditure: number
}

export interface PatientReportResponse {
  columns: ReportColumn[]
  rows: PatientReportRow[]
}

export interface PatientReportFilter {
  search?: string
  gender?: Gender
  goal?: Goal
  activityLevel?: ActivityLevel
}
