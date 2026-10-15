import type { PapelOrganizacional } from '../core/contexto/contexto'

export type StatusConvite = 'PENDENTE' | 'ACEITO' | 'EXPIRADO' | 'REVOGADO'

export interface Membro {
  vinculoId: number
  usuarioId: number
  nome: string
  email: string
  papel: PapelOrganizacional
  ativo: boolean
  desde: string
}

export interface Convite {
  id: number
  email: string
  papel: PapelOrganizacional
  status: StatusConvite
  expiraEm: string
  convidadoPorNome: string
  criadoEm: string
}

export interface ConviteCreateRequest {
  email: string
  papel: PapelOrganizacional
}

export interface ConviteAceiteRequest {
  token: string
  nome: string
  senha: string
}

export interface PacienteProfissional {
  id: number
  pacienteId: number
  pacienteNome: string
  profissionalId: number
  profissionalNome: string
  ativo: boolean
  criadoEm: string
}
