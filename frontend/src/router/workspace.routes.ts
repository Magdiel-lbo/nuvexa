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
  {
    path: '/configuracoes/minha-conta',
    name: 'configuracoes-minha-conta',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Minha conta' },
  },
  {
    path: '/configuracoes/preferencias',
    name: 'configuracoes-preferencias',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Preferências' },
  },
  {
    path: '/configuracoes/aparencia',
    name: 'configuracoes-aparencia',
    component: () => import('../pages/EmConstrucao.vue'),
    meta: { title: 'Aparência' },
  },
]

export default routes
