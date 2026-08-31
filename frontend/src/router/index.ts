import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import authRoutes from './auth.routes'
import workspaceRoutes from './workspace.routes'
import consultaRoutes from './consulta.routes'
import gestaoRoutes from './gestao.routes'
import relatoriosRoutes from './relatorios.routes'
import nutricaoRoutes from '../nutricao/routes/nutricao.routes'
import { useAuthStore } from '../core/auth/auth.store'
import { useContextoStore } from '../core/contexto/contexto.store'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    title?: string
    description?: string
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'dashboard',
    component: () => import('../pages/dashboard/Dashboard.vue'),
    meta: { title: 'Dashboard' },
  },
  ...nutricaoRoutes,
  ...consultaRoutes,
  ...workspaceRoutes,
  ...gestaoRoutes,
  ...relatoriosRoutes,
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

  return true
})

export default router
