import type { EnumOpcao } from '../../util/enum-rotulos'
import type { Sexo } from '../../types/paciente'

export type Objetivo = 'EMAGRECIMENTO' | 'MANUTENCAO_PESO' | 'GANHO_MASSA_MUSCULAR' | 'CONDICIONAMENTO_FISICO'

export type NivelAtividade = 'SEDENTARIO' | 'LEVEMENTE_ATIVO' | 'MODERADAMENTE_ATIVO' | 'MUITO_ATIVO' | 'EXTREMAMENTE_ATIVO'

export interface PerfilNutricionalResponse {
  pacienteId: number
  pacienteNome: string
  sexo: Sexo
  idade: number
  altura: number
  peso: number
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  caloriasDiariasManuais: number | null
  observacoes: string | null
  imc: number
  classificacaoImc: string
  taxaMetabolicaBasal: number
  gastoCaloricoDiario: number
  criadoEm: string
  atualizadoEm: string
}

export interface PerfilNutricionalCreateRequest {
  altura: number
  peso: number
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  caloriasDiariasManuais?: number | null
  observacoes?: string | null
}

export type PerfilNutricionalUpdateRequest = PerfilNutricionalCreateRequest

export interface PerfilNutricionalEnumsResponse {
  objetivos: EnumOpcao[]
  niveisAtividade: EnumOpcao[]
}
