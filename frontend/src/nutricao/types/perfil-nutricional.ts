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
  peso: number | null
  avaliacaoAtualId: number | null
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  caloriasDiariasManuais: number | null
  observacoes: string | null
  imc: number | null
  classificacaoImc: string | null
  taxaMetabolicaBasal: number | null
  gastoCaloricoDiario: number | null
  criadoEm: string
  atualizadoEm: string
}

export interface PerfilNutricionalCreateRequest {
  altura: number
  pesoInicial: number
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  caloriasDiariasManuais?: number | null
  observacoes?: string | null
}

export interface PerfilNutricionalUpdateRequest {
  altura: number
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  caloriasDiariasManuais?: number | null
  observacoes?: string | null
}

export interface PerfilNutricionalEnumsResponse {
  objetivos: EnumOpcao[]
  niveisAtividade: EnumOpcao[]
}
