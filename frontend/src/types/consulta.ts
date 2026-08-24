export type ConsultaStatus = 'AGENDADA' | 'CONFIRMADA' | 'REALIZADA' | 'CANCELADA' | 'FALTOU'

export type ConsultaTipo = 'PRIMEIRA_CONSULTA' | 'RETORNO' | 'AVALIACAO'

export interface Consulta {
  id: number
  pacienteId: number
  pacienteNome: string
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
}

export interface ConsultaCreateRequest {
  pacienteId: number
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
}

export interface ConsultaUpdateRequest {
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
}
