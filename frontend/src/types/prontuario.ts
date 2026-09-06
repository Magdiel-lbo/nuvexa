import type { EnumOpcao } from '../util/enum-rotulos'

export type SecaoProntuario = 'ANAMNESE' | 'EVOLUCAO' | 'EXAMES'

export type StatusProntuario = 'RASCUNHO' | 'PENDENTE' | 'ASSINADO'

export interface ProntuarioEnumsResponse {
  secoes: EnumOpcao[]
  status: EnumOpcao[]
}

export interface Prontuario {
  id: number
  registro: string
  pacienteId: number
  pacienteNome: string
  autorId: number
  autorNome: string
  secao: SecaoProntuario
  status: StatusProntuario
  conteudo: string | null
  comAnexo: boolean
  criadoEm: string
  atualizadoEm: string
}

export interface ProntuarioCreateRequest {
  pacienteId: number
  autorId: number
  secao: SecaoProntuario
  status: StatusProntuario
  conteudo?: string | null
  comAnexo?: boolean
}

export interface ProntuarioUpdateRequest {
  autorId: number
  secao: SecaoProntuario
  status: StatusProntuario
  conteudo?: string | null
  comAnexo?: boolean
}
