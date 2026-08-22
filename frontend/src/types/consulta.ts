export type ConsultaStatus = 'AGENDADA' | 'CONFIRMADA' | 'REALIZADA' | 'CANCELADA' | 'FALTOU'

export type ConsultaTipo = 'PRIMEIRA_CONSULTA' | 'RETORNO' | 'AVALIACAO'

export interface Consulta {
  id: number
  patientId: number
  patientName: string
  date: string
  durationMinutes: number
  type: ConsultaTipo
  status: ConsultaStatus
  notes: string | null
}
