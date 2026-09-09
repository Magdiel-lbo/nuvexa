import type { EnumOpcao } from '../util/enum-rotulos'

export type SecaoProntuario = 'ANAMNESE' | 'EVOLUCAO' | 'EXAMES'

export type StatusProntuario = 'RASCUNHO' | 'PENDENTE' | 'ASSINADO'

export type TipoEventoAuditoria = 'CRIACAO' | 'EDICAO' | 'ASSINATURA' | 'ADENDO' | 'EXCLUSAO' | 'UPLOAD_ANEXO' | 'EXCLUSAO_ANEXO'

export interface ProntuarioEnumsResponse {
  secoes: EnumOpcao[]
  status: EnumOpcao[]
  eventosAuditoria: EnumOpcao[]
}

export interface ProntuarioAdendo {
  id: number
  prontuarioId: number
  autorId: number
  autorNome: string
  texto: string
  criadoEm: string
}

export interface ProntuarioAdendoCreateRequest {
  autorId: number
  texto: string
}

export interface ProntuarioAnexo {
  id: number
  nomeOriginal: string
  tipoMime: string
  tamanho: number
  criadoPorId: number
  criadoPorNome: string
  criadoEm: string
}

export interface EventoAuditoria {
  id: number
  tipoEvento: TipoEventoAuditoria
  usuarioId: number
  usuarioNome: string
  dadosAntes: string | null
  dadosDepois: string | null
  criadoEm: string
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
  assinadoPorId: number | null
  assinadoPorNome: string | null
  assinadoEm: string | null
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

export interface ProntuarioRelatorioFiltro {
  busca?: string
  status?: string
  secao?: string
  periodo?: string
  autorId?: string
  anexo?: string
}
