import type { ConsultaStatus } from '../types/consulta'

const STATUS_COLORS: Record<ConsultaStatus, string> = {
  AGENDADA: 'on-surface-variant',
  CONFIRMADA: 'info',
  REALIZADA: 'success',
  CANCELADA: 'error',
  FALTOU: 'error',
}

export function consultaStatusColor(status: ConsultaStatus): string {
  return STATUS_COLORS[status]
}
