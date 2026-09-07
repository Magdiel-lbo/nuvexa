import type { EnumOpcao } from '../util/enum-rotulos'

export type Sexo = 'MASCULINO' | 'FEMININO'

export interface PacienteResponse {
  id: number
  nome: string
  dataNascimento: string
  sexo: Sexo
  idade: number
  criadoEm: string
  atualizadoEm: string
}

export interface PacienteCreateRequest {
  nome: string
  dataNascimento: string
  sexo: Sexo
}

export type PacienteUpdateRequest = PacienteCreateRequest

export interface PacienteEnumsResponse {
  sexos: EnumOpcao[]
}
