export const PERIODO_VALUES = ['hoje', '7d', '30d', '90d', 'ano'] as const

export type PeriodoValor = (typeof PERIODO_VALUES)[number]

const CHAVES_ROTULO: Record<PeriodoValor, string> = {
  hoje: 'periodoFiltro.hoje',
  '7d': 'periodoFiltro.ultimos7Dias',
  '30d': 'periodoFiltro.ultimos30Dias',
  '90d': 'periodoFiltro.ultimos90Dias',
  ano: 'periodoFiltro.esteAno',
}

export function periodoLabelKey(valor: string): string {
  return valor in CHAVES_ROTULO ? CHAVES_ROTULO[valor as PeriodoValor] : valor
}

function isSameDay(a: Date, b: Date): boolean {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate()
}

export function dentroDoPeriodo(dataIso: string, periodo: string): boolean {
  if (!periodo) return true
  const data = new Date(dataIso)
  const agora = new Date()
  const diffDias = (agora.getTime() - data.getTime()) / 86400000
  switch (periodo as PeriodoValor) {
    case 'hoje':
      return isSameDay(data, agora)
    case '7d':
      return diffDias >= 0 && diffDias <= 7
    case '30d':
      return diffDias >= 0 && diffDias <= 30
    case '90d':
      return diffDias >= 0 && diffDias <= 90
    case 'ano':
      return data.getFullYear() === agora.getFullYear()
    default:
      return true
  }
}
