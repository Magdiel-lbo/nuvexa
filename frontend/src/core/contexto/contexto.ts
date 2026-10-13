export type PapelOrganizacional = 'PROPRIETARIO' | 'GESTOR' | 'MEMBRO'

/** Vertical clínica da organização — espelha `Especialidade` do backend. */
export type Especialidade = 'NUTRICAO'

export interface ContextoResponse {
  usuarioId: number
  nome: string
  email: string
  /** Papel de acesso à plataforma. */
  perfil: string
  organizacaoId: number
  organizacaoNome: string
  organizacaoTipo: string
  especialidade: Especialidade
  /** Papel dentro da organização. */
  papelOrganizacional: PapelOrganizacional
}
