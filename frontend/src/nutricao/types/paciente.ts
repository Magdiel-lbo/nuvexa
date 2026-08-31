export type Sexo = 'MASCULINO' | 'FEMININO'

export type Objetivo =
  | 'EMAGRECIMENTO'
  | 'MANUTENCAO_PESO'
  | 'GANHO_MASSA_MUSCULAR'
  | 'CONDICIONAMENTO_FISICO'

export type NivelAtividade =
  | 'SEDENTARIO'
  | 'LEVEMENTE_ATIVO'
  | 'MODERADAMENTE_ATIVO'
  | 'MUITO_ATIVO'
  | 'EXTREMAMENTE_ATIVO'

export interface PacienteResponse {
  id: number
  nome: string
  dataNascimento: string
  sexo: Sexo
  altura: number
  peso: number
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  caloriasDiariasManuais: number | null
  observacoes: string | null
  criadoEm: string
  atualizadoEm: string
  idade: number
  imc: number
  classificacaoImc: string
  taxaMetabolicaBasal: number
  gastoCaloricoDiario: number
}

export interface PacienteCreateRequest {
  nome: string
  dataNascimento: string
  sexo: Sexo
  altura: number
  peso: number
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  caloriasDiariasManuais?: number | null
  observacoes?: string | null
}

export type PacienteUpdateRequest = PacienteCreateRequest

export interface EnumOpcao {
  valor: string
  rotulo: string
}

export interface PacienteEnumsResponse {
  sexos: EnumOpcao[]
  objetivos: EnumOpcao[]
  niveisAtividade: EnumOpcao[]
}

export interface RelatorioColuna {
  chave: string
  rotulo: string
  ordem: number
}

export interface PacienteRelatorioLinha {
  id: number
  nome: string
  idade: number
  sexo: Sexo
  objetivo: Objetivo
  nivelAtividade: NivelAtividade
  imc: number
  classificacaoImc: string
  gastoCaloricoDiario: number
}

export interface PacienteRelatorioResponse {
  colunas: RelatorioColuna[]
  linhas: PacienteRelatorioLinha[]
}

export interface PacienteRelatorioFiltro {
  busca?: string
  sexo?: Sexo
  objetivo?: Objetivo
  nivelAtividade?: NivelAtividade
}
