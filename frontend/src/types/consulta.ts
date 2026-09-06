export type ConsultaStatus = 'AGENDADA' | 'CONFIRMADA' | 'REALIZADA' | 'CANCELADA' | 'FALTOU'

export type ConsultaTipo = 'PRIMEIRA_CONSULTA' | 'RETORNO' | 'AVALIACAO'

export interface Profissional {
  id: number
  nome: string
}

export interface Consulta {
  id: number
  pacienteId: number
  pacienteNome: string
  profissionalId: number
  profissionalNome: string
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
}

export interface ConsultaCreateRequest {
  pacienteId: number
  profissionalId: number
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
}

export interface ConsultaUpdateRequest {
  profissionalId: number
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
}

export interface ConsultaRelatorioFiltro {
  busca?: string
  status?: string
  tipo?: string
  periodo?: string
  profissionalId?: string
}
