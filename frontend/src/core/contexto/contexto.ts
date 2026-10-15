export type PapelOrganizacional = 'PROPRIETARIO' | 'GESTOR' | 'MEMBRO' | 'RECEPCAO'

/** Vertical clínica da organização — espelha `Especialidade` do backend. */
export type Especialidade = 'NUTRICAO' | 'TERAPIA'

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

/** Organização em que o usuário tem vínculo ativo — as que ele pode escolher (cabeçalho X-Organizacao-Id). */
export interface OrganizacaoDoUsuario {
  organizacaoId: number
  nome: string
  tipo: string
  especialidade: Especialidade
  papel: PapelOrganizacional
}
