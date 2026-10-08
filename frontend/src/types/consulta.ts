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
  motivo: string | null
  criadoEm: string
  atualizadoEm: string
}

export interface ConsultaCreateRequest {
  pacienteId: number
  profissionalId: number
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
  motivo: string | null
  // Motivo da transição de status (null->status inicial) — obrigatório no backend quando status
  // é CANCELADA ou FALTOU. Não confundir com `motivo` acima, que é o motivo/razão da consulta.
  motivoTransicao: string | null
}

export interface ConsultaUpdateRequest {
  profissionalId: number
  dataHora: string
  duracaoMinutos: number
  tipo: ConsultaTipo
  status: ConsultaStatus
  observacoes: string | null
  motivo: string | null
  // Motivo da transição de status — obrigatório no backend quando o status muda para CANCELADA
  // ou FALTOU; ignorado quando o status não muda. Não confundir com `motivo` acima.
  motivoTransicao: string | null
}

export interface ConsultaRelatorioFiltro {
  busca?: string
  status?: string
  tipo?: string
  periodo?: string
  profissionalId?: string
}
