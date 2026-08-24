import { verticalRegistry } from '../../core/verticais/vertical-registry'
import nutricaoRoutes from './routes/nutricao.routes'

/**
 * Espelha `verticals.nutricao.NutricaoStrategy` do backend: a vertical se descreve e se
 * registra sozinha. Importar este módulo (por efeito colateral) é o que a torna conhecida
 * pelo `verticalRegistry` — feito uma única vez, no bootstrap (`main.ts`).
 */
verticalRegistry.registrar({
  especialidade: 'NUTRICAO',
  nome: 'Nutrição',
  rotaBase: '/pacientes',
  icone: 'mdi-account-group-outline',
  rotas: nutricaoRoutes,
})
