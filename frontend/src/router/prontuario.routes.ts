import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/prontuarios',
    name: 'prontuarios',
    component: () => import('../pages/prontuarios/ProntuarioLista.vue'),
    meta: { title: 'Prontuários' },
  },
  {
    path: '/prontuarios/novo',
    name: 'prontuario-novo',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Novo registro' },
  },
  {
    path: '/prontuarios/:id/editar',
    name: 'prontuario-editar',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Editar registro' },
  },
  {
    path: '/prontuarios/:id',
    name: 'prontuario-visualizar',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Prontuário' },
  },
]

export default routes
