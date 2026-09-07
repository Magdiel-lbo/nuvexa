import type { Sexo } from '../../types/paciente'
import type { NivelAtividade, Objetivo } from './perfil-nutricional'

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
