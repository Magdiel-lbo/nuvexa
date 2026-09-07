import type { EnumOpcao } from '../../util/enum-rotulos'

export type StatusPlanoAlimentar = 'ATIVO' | 'RASCUNHO' | 'ENCERRADO'

export interface PlanoAlimentarEnumsResponse {
  status: EnumOpcao[]
}

export interface PlanoAlimentar {
  id: number
  pacienteId: number
  pacienteNome: string
  autorId: number
  autorNome: string
  nome: string
  dataInicio: string
  calorias: number
  refeicoesPorDia: number
  status: StatusPlanoAlimentar
  criadoEm: string
  atualizadoEm: string
}

export interface PlanoAlimentarCreateRequest {
  pacienteId: number
  autorId: number
  nome: string
  dataInicio: string
  calorias: number
  refeicoesPorDia: number
  status: StatusPlanoAlimentar
}

export interface PlanoAlimentarUpdateRequest {
  autorId: number
  nome: string
  dataInicio: string
  calorias: number
  refeicoesPorDia: number
  status: StatusPlanoAlimentar
}

export interface PlanoAlimentarRelatorioFiltro {
  busca?: string
  status?: string
  plano?: string
  faixaCalorica?: string
  refeicoesPorDia?: string
}
