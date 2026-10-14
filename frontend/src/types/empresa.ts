export type EmpresaTipo = 'INDIVIDUAL' | 'CLINICA'

export type EmpresaStatus = 'ATIVA' | 'SUSPENSA'

export interface Empresa {
  id: number
  nome: string
  razaoSocial: string | null
  documento: string | null
  tipo: EmpresaTipo
  especialidade: string
  status: EmpresaStatus
  criadoEm: string
  atualizadoEm: string
}

export interface EmpresaOpcao {
  valor: string
  rotulo: string
}

export interface EmpresaEnums {
  tipos: EmpresaOpcao[]
  especialidades: EmpresaOpcao[]
  status: EmpresaOpcao[]
}
