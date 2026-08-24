import type { Especialidade } from './especialidade'
import type { DescritorDeVertical } from './descritor-de-vertical'

/**
 * Espelha `VerticalRegistry` do backend: só descobre/indexa verticais já registradas,
 * nunca decide regra de negócio. Cada vertical se registra chamando `registrar()` no seu
 * próprio módulo (ver `verticals/nutricao/nutricao.vertical.ts`), importado uma única vez
 * no bootstrap (`main.ts`) — antes do router ser montado.
 */
const verticaisPorEspecialidade = new Map<Especialidade, DescritorDeVertical>()

function registrar(descritor: DescritorDeVertical): void {
  if (verticaisPorEspecialidade.has(descritor.especialidade)) {
    throw new Error(`Mais de uma vertical registrada para a especialidade ${descritor.especialidade}`)
  }
  verticaisPorEspecialidade.set(descritor.especialidade, descritor)
}

function buscarPorEspecialidade(especialidade: Especialidade): DescritorDeVertical | undefined {
  return verticaisPorEspecialidade.get(especialidade)
}

function listarDisponiveis(): DescritorDeVertical[] {
  return [...verticaisPorEspecialidade.values()]
}

export const verticalRegistry = { registrar, buscarPorEspecialidade, listarDisponiveis }
