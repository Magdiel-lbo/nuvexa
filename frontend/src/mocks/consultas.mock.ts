import type { Consulta } from '../types/consulta'

function atDay(daysFromToday: number, hour: number, minute: number): string {
  const date = new Date()
  date.setDate(date.getDate() + daysFromToday)
  date.setHours(hour, minute, 0, 0)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`
}

export const mockConsultas: Consulta[] = [
  { id: 1, patientId: 1, patientName: 'João da Silva', date: atDay(0, 9, 0), durationMinutes: 30, type: 'RETORNO', status: 'CONFIRMADA', notes: null },
  { id: 2, patientId: 2, patientName: 'Maria Oliveira', date: atDay(0, 10, 30), durationMinutes: 45, type: 'AVALIACAO', status: 'AGENDADA', notes: null },
  { id: 3, patientId: 6, patientName: 'Pedro Henrique Alves', date: atDay(0, 14, 0), durationMinutes: 30, type: 'RETORNO', status: 'CONFIRMADA', notes: null },
  { id: 4, patientId: 9, patientName: 'Camila Ferreira', date: atDay(1, 8, 30), durationMinutes: 60, type: 'PRIMEIRA_CONSULTA', status: 'AGENDADA', notes: 'Encaminhada pelo cardiologista.' },
  { id: 5, patientId: 3, patientName: 'Carlos Pereira', date: atDay(1, 11, 0), durationMinutes: 30, type: 'RETORNO', status: 'AGENDADA', notes: null },
  { id: 6, patientId: 4, patientName: 'Ana Beatriz Costa', date: atDay(2, 9, 30), durationMinutes: 30, type: 'AVALIACAO', status: 'CONFIRMADA', notes: null },
  { id: 7, patientId: 7, patientName: 'Juliana Santos', date: atDay(2, 15, 30), durationMinutes: 45, type: 'PRIMEIRA_CONSULTA', status: 'AGENDADA', notes: null },
  { id: 8, patientId: 1, patientName: 'João da Silva', date: atDay(-1, 9, 0), durationMinutes: 30, type: 'RETORNO', status: 'REALIZADA', notes: null },
  { id: 9, patientId: 5, patientName: 'Fernanda Lima', date: atDay(-2, 16, 0), durationMinutes: 30, type: 'RETORNO', status: 'FALTOU', notes: null },
  { id: 10, patientId: 8, patientName: 'Ricardo Souza', date: atDay(-3, 10, 0), durationMinutes: 30, type: 'RETORNO', status: 'CANCELADA', notes: 'Paciente remarcou.' },
  { id: 11, patientId: 10, patientName: 'Bruno Martins', date: atDay(4, 13, 0), durationMinutes: 30, type: 'RETORNO', status: 'AGENDADA', notes: null },
  { id: 12, patientId: 2, patientName: 'Maria Oliveira', date: atDay(6, 9, 0), durationMinutes: 30, type: 'RETORNO', status: 'AGENDADA', notes: null },
]
