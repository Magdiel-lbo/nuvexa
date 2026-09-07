import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/nutricao',
    name: 'nutricao',
    component: () => import('../pages/plano-alimentar/PlanoAlimentarLista.vue'),
    meta: { title: 'Nutrição' },
  },
  {
    path: '/nutricao/novo',
    name: 'nutricao-novo',
    component: () => import('../pages/plano-alimentar/PlanoAlimentarFormulario.vue'),
    meta: { title: 'Novo plano' },
  },
  {
    path: '/nutricao/:id/editar',
    name: 'nutricao-editar',
    component: () => import('../pages/plano-alimentar/PlanoAlimentarFormulario.vue'),
    meta: { title: 'Editar plano' },
  },
  {
    path: '/nutricao/:id',
    name: 'nutricao-visualizar',
    component: () => import('../pages/plano-alimentar/PlanoAlimentarFormulario.vue'),
    meta: { title: 'Plano alimentar' },
  },
  {
    path: '/avaliacoes',
    name: 'avaliacoes',
    component: () => import('../pages/avaliacao/AvaliacaoLista.vue'),
    meta: { title: 'Avaliações' },
  },
  {
    path: '/avaliacoes/novo',
    name: 'avaliacao-novo',
    component: () => import('../pages/avaliacao/AvaliacaoFormulario.vue'),
    meta: { title: 'Nova avaliação' },
  },
  {
    path: '/avaliacoes/:id/editar',
    name: 'avaliacao-editar',
    component: () => import('../pages/avaliacao/AvaliacaoFormulario.vue'),
    meta: { title: 'Editar avaliação' },
  },
  {
    path: '/avaliacoes/:id',
    name: 'avaliacao-visualizar',
    component: () => import('../pages/avaliacao/AvaliacaoFormulario.vue'),
    meta: { title: 'Avaliação' },
  },
]

export default routes
