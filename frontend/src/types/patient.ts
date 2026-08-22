export type Gender = 'MASCULINO' | 'FEMININO'

export type Goal =
  | 'EMAGRECIMENTO'
  | 'MANUTENCAO_PESO'
  | 'GANHO_MASSA_MUSCULAR'
  | 'CONDICIONAMENTO_FISICO'

export type ActivityLevel =
  | 'SEDENTARIO'
  | 'LEVEMENTE_ATIVO'
  | 'MODERADAMENTE_ATIVO'
  | 'MUITO_ATIVO'
  | 'EXTREMAMENTE_ATIVO'

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
