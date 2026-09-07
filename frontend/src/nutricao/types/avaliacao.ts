import type { EnumOpcao } from '../../util/enum-rotulos'

export type TipoAvaliacao = 'BIOIMPEDANCIA' | 'ANTROPOMETRIA' | 'DOBRAS_CUTANEAS'

export type StatusAvaliacao = 'AGENDADA' | 'CONCLUIDA'

export interface AvaliacaoEnumsResponse {
  tipos: EnumOpcao[]
  status: EnumOpcao[]
}

export interface Avaliacao {
  id: number
  pacienteId: number
  pacienteNome: string
  avaliadorId: number
  avaliadorNome: string
  data: string
  tipo: TipoAvaliacao
  status: StatusAvaliacao
  peso: number | null
  percentualGordura: number | null
  variacaoPeso: number | null
  criadoEm: string
  atualizadoEm: string
}

export interface AvaliacaoCreateRequest {
  pacienteId: number
  avaliadorId: number
  data: string
  tipo: TipoAvaliacao
  status: StatusAvaliacao
  peso: number | null
  percentualGordura: number | null
}

export interface AvaliacaoUpdateRequest {
  avaliadorId: number
  data: string
  tipo: TipoAvaliacao
  status: StatusAvaliacao
  peso: number | null
  percentualGordura: number | null
}

export interface AvaliacaoRelatorioFiltro {
  busca?: string
  tipo?: string
  status?: string
  periodo?: string
  avaliadorId?: string
  tendencia?: string
}
