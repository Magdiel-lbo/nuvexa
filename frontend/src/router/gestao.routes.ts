import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/empresa',
    name: 'empresa',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Empresa' },
  },
  {
    path: '/usuarios',
    name: 'usuarios',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Usuários' },
  },
  {
    path: '/vinculos-permissoes',
    name: 'vinculos-permissoes',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Vínculos e permissões' },
  },
  {
    path: '/notificacoes',
    name: 'notificacoes',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Notificações' },
  },
  {
    path: '/integracoes',
    name: 'integracoes',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Integrações' },
  },
  {
    path: '/ia',
    name: 'ia',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'IA' },
  },
  {
    path: '/financeiro',
    name: 'financeiro',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Financeiro' },
  },
]

export default routes
