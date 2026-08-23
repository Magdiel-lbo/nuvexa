import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import pacienteRoutes from './paciente.routes'
import authRoutes from './auth.routes'
import workspaceRoutes from './workspace.routes'
import consultaRoutes from './consulta.routes'
import { useAuthStore } from '../store/auth.store'

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
  ...pacienteRoutes,
  ...consultaRoutes,
  ...workspaceRoutes,
  ...authRoutes,
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const isPublic = to.meta.public === true

  if (!isPublic && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (authStore.isAuthenticated && (to.name === 'login' || to.name === 'cadastrar')) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
