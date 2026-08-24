import type { RouteRecordRaw } from 'vue-router'
import type { Especialidade } from './especialidade'

/**
 * Espelha `DescritorDeVertical` do backend (core.vertical): metadado pequeno de
 * identidade/navegação — nunca uma descrição de tela/workflow completa. `rotas` é a
 * única concessão além do equivalente Java, porque o router do frontend precisa montar
 * as rotas a partir de algum lugar; continua sendo dado de roteamento, não de UI.
 */
export interface DescritorDeVertical {
  especialidade: Especialidade
  nome: string
  rotaBase: string
  icone: string
  rotas: RouteRecordRaw[]
}
