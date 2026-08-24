export type PapelOrganizacional = 'PROPRIETARIO' | 'GESTOR' | 'MEMBRO'

export interface ContextoResponse {
  usuarioId: number
  nome: string
  email: string
  /** Papel de acesso à plataforma. */
  perfil: string
  organizacaoId: number
  organizacaoNome: string
  organizacaoTipo: string
  /** Papel dentro da organização. */
  papelOrganizacional: PapelOrganizacional
}
