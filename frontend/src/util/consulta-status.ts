import type { ConsultaStatus } from '../types/consulta'

const STATUS_COLORS: Record<ConsultaStatus, string> = {
  AGENDADA: 'secondary',
  CONFIRMADA: 'success',
  REALIZADA: 'primary',
  CANCELADA: 'error',
  FALTOU: 'warning',
}

export function consultaStatusColor(status: ConsultaStatus): string {
  return STATUS_COLORS[status]
}
