import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import authRoutes from './auth.routes'
import workspaceRoutes from './workspace.routes'
import pacienteRoutes from './paciente.routes'
import consultaRoutes from './consulta.routes'
import prontuarioRoutes from './prontuario.routes'
import gestaoRoutes from './gestao.routes'
import nutricaoRoutes from '../nutricao/routes/nutricao.routes'
import { useAuthStore } from '../core/auth/auth.store'
import { useContextoStore } from '../core/contexto/contexto.store'
import type { Especialidade } from '../core/contexto/contexto'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    title?: string
    description?: string
    /** Rota exclusiva de uma vertical: quem não tem essa especialidade volta ao dashboard. Conveniência — o backend barra de fato. */
    especialidade?: Especialidade
    /** Rota só do perfil de plataforma ADMIN. */
    perfil?: 'ADMIN'
    /** Rota que exige uma permissão do papel na organização atual. Conveniência — o backend barra de fato. */
    permissao?: 'gerenciarEquipe' | 'editarOrganizacao'
    /** Rota de dado clínico: a recepção não acessa. */
    clinico?: boolean
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'dashboard',
    component: () => import('../pages/dashboard/Dashboard.vue'),
    meta: { title: 'Dashboard' },
  },
  ...nutricaoRoutes.map((rota) => ({ ...rota, meta: { ...rota.meta, especialidade: 'NUTRICAO' as const, clinico: true } })),
  ...pacienteRoutes,
  ...consultaRoutes,
  ...prontuarioRoutes.map((rota) => ({ ...rota, meta: { ...rota.meta, clinico: true } })),
  ...workspaceRoutes,
  ...gestaoRoutes,
  ...authRoutes,
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  const contextoStore = useContextoStore()
  const isPublic = to.meta.public === true

  if (!isPublic && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (authStore.isAuthenticated && (to.name === 'login' || to.name === 'cadastrar')) {
    return { name: 'dashboard' }
  }

  // Carrega o contexto uma vez por sessão do app — inclusive após F5, quando só o token sobrevive.
  if (!isPublic && authStore.isAuthenticated && !contextoStore.contexto) {
    try {
      await contextoStore.carregar()
    } catch {
      // 401 já é tratado pelo interceptor; 403 (usuário sem vínculo) não deve travar a navegação.
    }
  }

  const especialidadeExigida = to.meta.especialidade
  if (especialidadeExigida && contextoStore.contexto && contextoStore.contexto.especialidade !== especialidadeExigida) {
    return { name: 'dashboard' }
  }

  const contexto = contextoStore.contexto
  if (contexto) {
    const negado =
      (to.meta.perfil === 'ADMIN' && !contextoStore.isAdmin) ||
      (to.meta.permissao === 'gerenciarEquipe' && !contextoStore.podeGerenciarEquipe) ||
      (to.meta.permissao === 'editarOrganizacao' && !contextoStore.podeEditarOrganizacao) ||
      (to.meta.clinico && !contextoStore.podeVerClinico)
    if (negado) {
      return { name: 'dashboard' }
    }
  }

  return true
})

export default router
