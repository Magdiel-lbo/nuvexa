import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/agenda',
    name: 'agenda',
    component: () => import('../pages/agenda/Agenda.vue'),
    meta: { title: 'Agenda' },
  },
  {
    path: '/nutricao',
    name: 'nutricao',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Nutrição' },
  },
  {
    path: '/avaliacoes',
    name: 'avaliacoes',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Avaliações' },
  },
  {
    path: '/configuracoes',
    name: 'configuracoes',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Configurações' },
  },
]

export default routes
